param()

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest
$repoRoot = Split-Path -Parent $PSScriptRoot
$launcherPath = Join-Path $repoRoot "importar_atoms.ps1"
$parseErrors = $null
[System.Management.Automation.Language.Parser]::ParseFile($launcherPath, [ref]$null, [ref]$parseErrors) | Out-Null
if ($parseErrors) { throw "El lanzador contiene errores de sintaxis: $parseErrors" }
$testRoot = Join-Path $repoRoot ("target\launcher-routing-tests\" + [guid]::NewGuid().ToString("N"))
New-Item -ItemType Directory -Force -Path $testRoot | Out-Null
$powershellExe = (Get-Process -Id $PID).Path
$utf8 = [System.Text.UTF8Encoding]::new($false)
$mockJavaPath = Join-Path $testRoot "mock-java.ps1"
$mockJavaSource = @'
param([Parameter(ValueFromRemainingArguments = $true)][string[]]$JavaArgs)
$ErrorActionPreference = "Stop"
$configArgument = @($JavaArgs | Where-Object { $_.StartsWith("--configDir=") })[0]
$configDir = $configArgument.Substring("--configDir=".Length)
$databaseText = [System.IO.File]::ReadAllText((Join-Path $configDir "bd.properties"))
$hibernateText = [System.IO.File]::ReadAllText((Join-Path $configDir "hibernate.properties"))
$jarIndex = [array]::IndexOf($JavaArgs, "-jar")
[ordered]@{
    databaseText = $databaseText
    ddl = [regex]::Match($hibernateText, '(?m)^hibernate\.hbm2ddl\.auto=([^\r\n]+)').Groups[1].Value
    jar = [System.IO.Path]::GetFileName($JavaArgs[$jarIndex + 1])
} | ConvertTo-Json -Compress | Add-Content -LiteralPath (Join-Path (Split-Path -Parent $configDir) "java-calls.jsonl") -Encoding UTF8
exit 0
'@
[System.IO.File]::WriteAllText($mockJavaPath, $mockJavaSource, $utf8)
$source = [System.IO.File]::ReadAllText($launcherPath)
$preferredJavaDeclaration = '$PreferredJavaExe = "C:\java\software\jdk-21.0.11\bin\java.exe"'
if (-not $source.Contains($preferredJavaDeclaration)) { throw "No se puede sustituir Java por el simulador en la copia de pruebas." }
$testLauncherPath = Join-Path $testRoot "importar_atoms.ps1"
$testSource = $source.Replace($preferredJavaDeclaration, '$PreferredJavaExe = ''' + $mockJavaPath.Replace("'", "''") + "'")
[System.IO.File]::WriteAllText($testLauncherPath, $testSource, $utf8)

function Assert-Equal {
    param($Actual, $Expected, [string]$Message)
    if ($Actual -cne $Expected) { throw $Message }
}

function Invoke-RoutingCase {
    param(
        [string]$Name,
        [string]$Url,
        [string[]]$Options,
        [string[]]$ExpectedDatabases,
        [string[]]$ExpectedDdl,
        [switch]$Dry,
        [switch]$Invalid,
        [switch]$MissingFilter
    )
    $caseDir = Join-Path $testRoot $Name
    $propertiesDir = Join-Path $caseDir "properties"
    New-Item -ItemType Directory -Force -Path $propertiesDir | Out-Null
    foreach ($jarName in @("opendata_con_filtros-8.2.0-local.jar", "opendata_con_filtros-8.2.0-internet.jar", "opendata_sin_filtros-8.2.0-local.jar", "opendata_sin_filtros-8.2.0-internet.jar")) {
        [System.IO.File]::WriteAllBytes((Join-Path $caseDir $jarName), [byte[]]@())
    }
    $databaseText = "# Configuracion de prueba`njakarta.persistence.jdbc.url=$Url`njakarta.persistence.jdbc.driver=org.mariadb.jdbc.Driver`njakarta.persistence.jdbc.user=previous_database`njakarta.persistence.jdbc.password=previous_database=clave&sin_cambios`n"
    $files = @{
        "bd.properties" = $databaseText
        "hibernate.properties" = "hibernate.hbm2ddl.auto=validate`nhibernate.jdbc.batch_size=20`n"
        "app.properties" = "app.tipo_sindicacion=MAYORES`napp.local.path=atoms\`napp.local.mayores=may.atom`napp.local.menores=men.atom`napp.local.cpms=cpm.atom`napp.local.emps=emp.atom`napp.internet.mayores=https://test.invalid/may`napp.email.enabled=false`n"
        "filter.properties" = $(if ($MissingFilter) { "filter.codigosPostales=`nfilter.nifs=`n" } else { "filter.codigosPostales=29`nfilter.nifs=`n" })
        "runtime.properties" = "java.opts=-Xms16m -Xmx32m`n"
    }
    foreach ($fileName in $files.Keys) { [System.IO.File]::WriteAllText((Join-Path $propertiesDir $fileName), $files[$fileName], $utf8) }
    $beforeHashes = @{}
    foreach ($fileName in $files.Keys) { $beforeHashes[$fileName] = (Get-FileHash -LiteralPath (Join-Path $propertiesDir $fileName)).Hash }
    $outputPath = Join-Path $caseDir "launcher.log"
    & $powershellExe -NoProfile -File $testLauncherPath -BaseDir $caseDir -SkipConfirmation @Options *> $outputPath
    $expectedExit = if ($Invalid -or $MissingFilter) { 1 } else { 0 }
    Assert-Equal $LASTEXITCODE $expectedExit "Codigo de salida incorrecto en $Name. Consulte $outputPath"
    $callsPath = Join-Path $caseDir "java-calls.jsonl"
    if ($Dry -or $Invalid -or $MissingFilter) {
        if (Test-Path -LiteralPath $callsPath) { throw "Se ejecuto Java cuando no debia en $Name" }
        foreach ($fileName in $files.Keys) {
            Assert-Equal (Get-FileHash -LiteralPath (Join-Path $propertiesDir $fileName)).Hash $beforeHashes[$fileName] "Se modificaron properties en $Name"
        }
        if ($Dry) {
            $output = [System.IO.File]::ReadAllText($outputPath)
            foreach ($database in ($ExpectedDatabases | Select-Object -Unique)) {
                if (-not $output.Contains("se seleccionaria la base de datos $database")) { throw "DryRun no muestra el destino $database" }
            }
            Assert-Equal ([regex]::Matches($output, 'se usaria hibernate.hbm2ddl.auto=create').Count) 2 "DryRun debe crear una vez cada base"
        }
    } else {
        $calls = @(Get-Content -LiteralPath $callsPath | ForEach-Object { $_ | ConvertFrom-Json })
        Assert-Equal $calls.Count $ExpectedDatabases.Count "Numero de bloques incorrecto en $Name"
        for ($index = 0; $index -lt $calls.Count; $index++) {
            $database = $ExpectedDatabases[$index]
            $expectedUrl = $Url.Replace("/previous_database", "/$database")
            Assert-Equal $calls[$index].databaseText ($databaseText.Replace($Url, $expectedUrl)) "URL, continuaciones o credenciales modificadas incorrectamente en $Name, bloque $index"
            Assert-Equal $calls[$index].ddl $ExpectedDdl[$index] "Modo DDL incorrecto en $Name, bloque $index"
            $expectedGroup = if ($database -eq "opendata_malaga") { "con_filtros" } else { "sin_filtros" }
            if (-not $calls[$index].jar.StartsWith("opendata_$expectedGroup-")) { throw "El JAR no coincide con su base en $Name" }
        }
    }
    Write-Output "OK: $Name"
}

$multilineUrl = 'jdbc:mariadb://localhost:3307/previous_database?useUnicode=true\' + "`n" + '&characterEncoding=utf8mb4\' + "`n" + '&serverTimezone=Europe/Madrid&password=a$1'
Invoke-RoutingCase -Name "con-filtros-local-cuatro-tipos" -Url $multilineUrl -Options @("-Grupo", "con_filtros", "-Mode", "local") -ExpectedDatabases @("opendata_malaga", "opendata_malaga", "opendata_malaga", "opendata_malaga") -ExpectedDdl @("validate", "validate", "validate", "validate")
Invoke-RoutingCase -Name "sin-filtros-local-sin-puerto" -Url "jdbc:mariadb://db.example/previous_database" -Options @("-Grupo", "sin_filtros", "-Mode", "local") -ExpectedDatabases @("opendata_pliegos") -ExpectedDdl @("validate")
Invoke-RoutingCase -Name "postgresql-internet" -Url "jdbc:postgresql://db.example:5433/previous_database?sslmode=require" -Options @("-Grupo", "con_filtros", "-Mode", "internet", "-TipoSindicacion", "MAYORES") -ExpectedDatabases @("opendata_malaga") -ExpectedDdl @("validate")
Invoke-RoutingCase -Name "ambos-grupos-ambos-modos" -Url $multilineUrl -Options @("-Grupo", "all", "-Mode", "all", "-TipoSindicacion", "MAYORES", "-CreateSchemaFirstRun") -ExpectedDatabases @("opendata_malaga", "opendata_malaga", "opendata_pliegos", "opendata_pliegos") -ExpectedDdl @("create", "none", "create", "none")
Invoke-RoutingCase -Name "dry-run-sin-cambios" -Url $multilineUrl -Options @("-Grupo", "all", "-Mode", "all", "-TipoSindicacion", "MAYORES", "-CreateSchemaFirstRun", "-DryRun") -ExpectedDatabases @("opendata_malaga", "opendata_pliegos") -Dry
Invoke-RoutingCase -Name "url-invalida-sin-importar" -Url "jdbc:h2:mem:testing" -Options @("-Grupo", "con_filtros", "-Mode", "local") -Invalid
Invoke-RoutingCase -Name "filtro-obligatorio-sin-importar" -Url $multilineUrl -Options @("-Grupo", "con_filtros", "-Mode", "local") -MissingFilter
Write-Output "7 casos correctos. Java simulado; no se conecto a ninguna base de datos. Evidencias: $testRoot"
