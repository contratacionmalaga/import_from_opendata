param(
    [string]$Grupo = "con_filtros",

    [string]$Mode = "local",

    [string[]]$TipoSindicacion,

    [switch]$DryRun,

    [switch]$ContinueOnError,

    [switch]$CreateSchemaFirstRun,

    [string]$BaseDir = "C:\java\ejecutables\import-from-opendata-ejecutables"
)

$OutputEncoding = [System.Text.Encoding]::UTF8
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$PreferredJavaExe = "C:\java\software\jdk-21.0.11\bin\java.exe"
$JavaExe = $PreferredJavaExe
$JavaOpts = @("-Xms12g", "-Xmx12g")

if ((-not (Test-Path -LiteralPath $BaseDir)) -and (Test-Path -LiteralPath (Join-Path $PSScriptRoot "properties"))) {
    $BaseDir = $PSScriptRoot
}

$logPath = Join-Path $BaseDir "logs"
if (-not (Test-Path -LiteralPath $logPath)) {
    New-Item -ItemType Directory -Force -Path $logPath | Out-Null
}

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$logFile = Join-Path $logPath "importacion_$timestamp.log"
$PropertiesDir = Join-Path $BaseDir "properties"
$PropertiesPath = Join-Path $PropertiesDir "app.properties"
$FilterPropertiesPath = Join-Path $PropertiesDir "filter.properties"
$HibernatePropertiesPath = Join-Path $PropertiesDir "hibernate.properties"
$DatabasePropertiesPath = Join-Path $PropertiesDir "bd.properties"

$AllowedGroups = @("con_filtros", "sin_filtros", "all")
$AllowedModes = @("local", "internet", "all")
$AllowedTipos = @("MAYORES", "MENORES", "CONSULTAS", "ENCARGOS", "AGREGRADAS")

$TiposPorGrupo = @{
    con_filtros = @("CONSULTAS", "ENCARGOS", "MAYORES", "MENORES")
    sin_filtros = @("MAYORES")
}

$ArtifactPorGrupo = @{
    con_filtros = "opendata_con_filtros"
    sin_filtros = "opendata_sin_filtros"
}

$TipoInfo = @{
    MAYORES = @{ LocalKey = "app.local.mayores"; InternetKey = "app.internet.mayores"; LocalSubdir = "may"; Nombre = "contratos mayores" }
    MENORES = @{ LocalKey = "app.local.menores"; InternetKey = "app.internet.menores"; LocalSubdir = "men"; Nombre = "contratos menores" }
    CONSULTAS = @{ LocalKey = "app.local.cpms"; InternetKey = "app.internet.cpms"; LocalSubdir = "cpm"; Nombre = "consultas preliminares de mercado" }
    ENCARGOS = @{ LocalKey = "app.local.emps"; InternetKey = "app.internet.emps"; LocalSubdir = "emp"; Nombre = "encargos a medios propios" }
    AGREGRADAS = @{ LocalKey = "app.local.agregadas"; InternetKey = "app.internet.agregadas"; LocalSubdir = "agr"; Nombre = "plataformas agregadas" }
}

function Log {
    param([string]$Message)
    $date = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    "$date - $Message" | Out-File -FilePath $logFile -Append -Encoding UTF8
}

function Show-Error {
    param([string]$Message, [string]$Hint = "")

    Write-Host ""
    Write-Host "No se puede iniciar la importacion" -ForegroundColor Yellow
    Write-Host "----------------------------------" -ForegroundColor DarkGray
    Write-Host $Message -ForegroundColor White
    if (-not [string]::IsNullOrWhiteSpace($Hint)) {
        Write-Host $Hint -ForegroundColor Gray
    }
    Write-Host ""
}

function Fail {
    param([string]$Message, [string]$Hint = "")

    Show-Error -Message $Message -Hint $Hint
    Log $Message
    exit 1
}

function Assert-ParameterValues {
    if ($Grupo -notin $AllowedGroups) {
        Fail "Grupo no valido: '$Grupo'." "Valores permitidos: $($AllowedGroups -join ', ')."
    }

    if ($Mode -notin $AllowedModes) {
        Fail "Modo no valido: '$Mode'." "Valores permitidos: $($AllowedModes -join ', ')."
    }

    if ($TipoSindicacion) {
        $invalidTipos = $TipoSindicacion | Where-Object { $_ -notin $AllowedTipos }
        if ($invalidTipos) {
            Fail "Tipo de sindicacion no reconocido: $($invalidTipos -join ', ')." "Valores permitidos: $($AllowedTipos -join ', ')."
        }
    }
}
function Test-PropertiesContinuation {
    param([string]$Line)
    $count = 0
    for ($i = $Line.Length - 1; $i -ge 0 -and $Line[$i] -eq '\'; $i--) {
        $count++
    }
    return ($count % 2) -eq 1
}

function Read-PropertiesMap {
    param([string]$Path)
    $map = @{}
    if (-not (Test-Path -LiteralPath $Path)) {
        return $map
    }

    $logicalLines = New-Object System.Collections.Generic.List[string]
    $current = ""
    foreach ($rawLine in Get-Content -LiteralPath $Path) {
        $line = $rawLine.Trim()
        if ($line.Length -eq 0 -or $line.StartsWith("#")) {
            continue
        }
        if (Test-PropertiesContinuation -Line $line) {
            $current += $line.Substring(0, $line.Length - 1)
            continue
        }
        $current += $line
        $logicalLines.Add($current)
        $current = ""
    }
    if ($current.Length -gt 0) {
        $logicalLines.Add($current)
    }

    foreach ($line in $logicalLines) {
        $index = $line.IndexOf("=")
        if ($index -le 0) {
            continue
        }
        $key = $line.Substring(0, $index).Trim()
        $value = $line.Substring($index + 1).Trim()
        $map[$key] = $value
    }
    return $map
}

function Get-PropertyOrDefault {
    param([hashtable]$Properties, [string]$Key, [string]$Default = "no indicado")
    if ($Properties.ContainsKey($Key) -and -not [string]::IsNullOrWhiteSpace($Properties[$Key])) {
        return $Properties[$Key]
    }
    return $Default
}

function Get-VersionFromJarName {
    param([System.IO.FileInfo]$Jar)
    if ($Jar.Name -match '^(?<Artifact>opendata_con_filtros|opendata_sin_filtros)-(?<Version>\d+(?:\.\d+){1,3})-(?<Mode>local|internet)\.jar$') {
        return [version]$Matches.Version
    }
    return [version]"0.0.0"
}

function Resolve-JarPath {
    param(
        [string]$GrupoImportacion,
        [string]$ModoImportacion
    )

    $artifact = $ArtifactPorGrupo[$GrupoImportacion]
    $pattern = "$artifact-*-$ModoImportacion.jar"
    $candidates = Get-ChildItem -LiteralPath $BaseDir -Filter $pattern -File |
        Where-Object { $_.Name -notmatch '-(sources|javadoc)\.jar$' } |
        Sort-Object @{ Expression = { Get-VersionFromJarName $_ }; Descending = $true }, LastWriteTime -Descending

    if (-not $candidates) {
        Fail "No se ha encontrado el ejecutable para '$GrupoImportacion' en modo '$ModoImportacion'." "Patron buscado: $pattern. Directorio revisado: $BaseDir"
    }
    return $candidates[0].FullName
}

function Set-PropertyValue {
    param([string[]]$Content, [string]$Key, [string]$Value)
    $escapedKey = [regex]::Escape($Key)
    $replacement = "$Key=$Value"
    $found = $false
    $updated = $Content | ForEach-Object {
        if ($_ -match "^\s*$escapedKey\s*=") {
            $found = $true
            $replacement
        } else {
            $_
        }
    }
    if (-not $found) {
        $updated += $replacement
    }
    return $updated
}

function Set-HibernateDdlMode {
    param([string]$ModeValue)
    $content = Get-Content -LiteralPath $HibernatePropertiesPath
    $updated = Set-PropertyValue -Content $content -Key "hibernate.hbm2ddl.auto" -Value $ModeValue
    $updated | Set-Content -LiteralPath $HibernatePropertiesPath -Encoding UTF8
}

function Set-TipoSindicacion {
    param([string]$Tipo)
    $content = Get-Content -LiteralPath $PropertiesPath
    $updated = Set-PropertyValue -Content $content -Key "app.tipo_sindicacion" -Value $Tipo
    $updated | Set-Content -LiteralPath $PropertiesPath -Encoding UTF8
}

function Resolve-JavaExe {
    if (Test-Path -LiteralPath $PreferredJavaExe) {
        return $PreferredJavaExe
    }
    $command = Get-Command java -ErrorAction SilentlyContinue
    if ($command) {
        return $command.Source
    }
    return $PreferredJavaExe
}

function Assert-Configuration {
    $script:JavaExe = Resolve-JavaExe
    if ((-not $DryRun) -and (-not (Test-Path -LiteralPath $JavaExe))) {
        Fail "ERROR: No se encuentra Java. Ruta preferida: $PreferredJavaExe"
    }
    foreach ($path in @($PropertiesPath, $FilterPropertiesPath, $HibernatePropertiesPath, $DatabasePropertiesPath)) {
        if (-not (Test-Path -LiteralPath $path)) {
            Fail "ERROR: No se encuentra el fichero de configuracion: $path"
        }
    }
}

function Get-SelectedGroups {
    if ($Grupo -eq "all") {
        return @("con_filtros", "sin_filtros")
    }
    return @($Grupo)
}

function Get-SelectedModes {
    if ($Mode -eq "all") {
        return @("local", "internet")
    }
    return @($Mode)
}

function Get-SelectedTipos {
    param([string]$GrupoImportacion)
    if ($TipoSindicacion -and $TipoSindicacion.Count -gt 0) {
        $invalid = $TipoSindicacion | Where-Object { $_ -notin $TiposPorGrupo[$GrupoImportacion] }
        if ($invalid) {
            Fail "Tipo de sindicacion no valido para '$GrupoImportacion': $($invalid -join ', ')." "Permitidos para este grupo: $($TiposPorGrupo[$GrupoImportacion] -join ', ')."
        }
        return $TipoSindicacion
    }
    return $TiposPorGrupo[$GrupoImportacion]
}

function Assert-RequiredFiltersForFilteredRun {
    param([object[]]$Plan)
    $requiresFilters = @($Plan | Where-Object { $_.Grupo -eq "con_filtros" }).Count -gt 0
    if (-not $requiresFilters) {
        return
    }

    $filters = Read-PropertiesMap -Path $FilterPropertiesPath
    $nifs = Get-PropertyOrDefault -Properties $filters -Key "filter.nifs" -Default ""
    $postalCodes = Get-PropertyOrDefault -Properties $filters -Key "filter.codigosPostales" -Default ""

    if ([string]::IsNullOrWhiteSpace($nifs) -and [string]::IsNullOrWhiteSpace($postalCodes)) {
        Fail "opendata_con_filtros requiere un filtro antes de importar." "Informa filter.nifs o filter.codigosPostales en $FilterPropertiesPath. No se ha ejecutado Java ni se ha modificado app.properties."
    }
}

function Build-ImportPlan {
    $plan = New-Object System.Collections.Generic.List[object]
    foreach ($selectedGroup in Get-SelectedGroups) {
        foreach ($selectedMode in Get-SelectedModes) {
            $jarPath = Resolve-JarPath -GrupoImportacion $selectedGroup -ModoImportacion $selectedMode
            $selectedTipos = Get-SelectedTipos -GrupoImportacion $selectedGroup
            foreach ($tipo in $selectedTipos) {
                $plan.Add([PSCustomObject]@{ Grupo = $selectedGroup; Mode = $selectedMode; Tipo = $tipo; JarPath = $jarPath })
            }
        }
    }
    return $plan
}

function Get-DatabaseSummary {
    param([hashtable]$DatabaseProperties)
    $url = Get-PropertyOrDefault -Properties $DatabaseProperties -Key "jakarta.persistence.jdbc.url"
    $driver = Get-PropertyOrDefault -Properties $DatabaseProperties -Key "jakarta.persistence.jdbc.driver"
    $user = Get-PropertyOrDefault -Properties $DatabaseProperties -Key "jakarta.persistence.jdbc.user"
    $summary = [ordered]@{ Motor = "no identificado"; Servidor = "no indicado"; Puerto = "no indicado"; BaseDeDatos = "no indicada"; Usuario = $user; Driver = $driver; Url = $url }
    if ($url -match '^jdbc:(?<motor>mariadb|postgresql)://(?<host>[^/:?]+)(:(?<port>\d+))?/(?<db>[^?]+)') {
        $summary.Motor = if ($Matches.motor -eq "mariadb") { "MariaDB" } else { "PostgreSQL" }
        $summary.Servidor = $Matches.host
        $summary.Puerto = if ($Matches.port) { $Matches.port } else { "por defecto" }
        $summary.BaseDeDatos = $Matches.db
    }
    return $summary
}

function Get-HibernateModeMessage {
    param([string]$ModeValue)
    switch ($ModeValue) {
        "validate" { return "Solo comprueba que la base de datos tiene la estructura esperada. No deberia cambiar tablas." }
        "update" { return "Puede ajustar la estructura de la base de datos si detecta diferencias." }
        "create" { return "ATENCION: puede borrar y crear de nuevo tablas al arrancar. Usarlo contra datos reales puede eliminar informacion existente." }
        "create-drop" { return "ATENCION: crea tablas y puede eliminarlas al terminar. Es destructivo para datos reales." }
        "none" { return "No cambia la estructura de la base de datos." }
        default { return "No se reconoce el modo. Revise hibernate.properties antes de continuar." }
    }
}

function Get-OriginDescription {
    param([hashtable]$AppProperties, [string]$ModeValue, [string]$Tipo)
    $info = $TipoInfo[$Tipo]
    if ($ModeValue -eq "local") {
        $basePath = (Get-PropertyOrDefault -Properties $AppProperties -Key "app.local.path").Replace("\\", "\")
        $fileName = Get-PropertyOrDefault -Properties $AppProperties -Key $info.LocalKey
        return "$basePath$($info.LocalSubdir)\$fileName"
    }
    return Get-PropertyOrDefault -Properties $AppProperties -Key $info.InternetKey
}

function Show-ImportPlanAndConfirm {
    param([object[]]$Plan)
    if ($DryRun) {
        return
    }

    $appProperties = Read-PropertiesMap -Path $PropertiesPath
    $filterProperties = Read-PropertiesMap -Path $FilterPropertiesPath
    $hibernateProperties = Read-PropertiesMap -Path $HibernatePropertiesPath
    $databaseProperties = Read-PropertiesMap -Path $DatabasePropertiesPath
    $db = Get-DatabaseSummary -DatabaseProperties $databaseProperties
    $ddlMode = Get-PropertyOrDefault -Properties $hibernateProperties -Key "hibernate.hbm2ddl.auto"
    $batchSize = Get-PropertyOrDefault -Properties $hibernateProperties -Key "hibernate.jdbc.batch_size"
    $poolSize = Get-PropertyOrDefault -Properties $hibernateProperties -Key "hibernate.hikari.maximumPoolSize"
    $statistics = Get-PropertyOrDefault -Properties $hibernateProperties -Key "hibernate.generate_statistics"
    $persistRejected = Get-PropertyOrDefault -Properties $appProperties -Key "app.persistir_historicos_rechazados"
    $httpRetries = Get-PropertyOrDefault -Properties $appProperties -Key "app.http.max_retries"
    $httpDelay = Get-PropertyOrDefault -Properties $appProperties -Key "app.http.retry_delay_ms"
    $nifs = Get-PropertyOrDefault -Properties $filterProperties -Key "filter.nifs" -Default "sin filtro"
    $postalCodes = Get-PropertyOrDefault -Properties $filterProperties -Key "filter.codigosPostales" -Default "sin filtro"

    Write-Host "`n=== Revision antes de importar ===" -ForegroundColor Cyan
    Write-Host "Se ha validado que los parametros indicados son coherentes y que existe el ejecutable correspondiente." -ForegroundColor Gray
    Write-Host "`nDestino de los datos" -ForegroundColor Yellow
    Write-Host "- Se escribira en la base de datos '$($db.BaseDeDatos)' de $($db.Motor), servidor $($db.Servidor), puerto $($db.Puerto)."
    Write-Host "- Usuario configurado: $($db.Usuario). La contrasena no se muestra."
    Write-Host "- Fichero de conexion: $DatabasePropertiesPath"
    Write-Host "`nConfiguracion de Hibernate" -ForegroundColor Yellow
    Write-Host "- Modo de preparacion de tablas actual: $ddlMode. $(Get-HibernateModeMessage -ModeValue $ddlMode)"
    if ($CreateSchemaFirstRun) {
        Write-Host "- Secuencia solicitada: primera importacion con hibernate.hbm2ddl.auto=create; siguientes importaciones con hibernate.hbm2ddl.auto=none."
    }
    Write-Host "- Trabajo por lotes: hasta $batchSize operaciones juntas para ir mas rapido."
    Write-Host "- Conexiones simultaneas maximas a la base de datos: $poolSize."
    Write-Host "- Estadisticas internas activadas: $statistics."
    Write-Host "`nFiltros configurados" -ForegroundColor Yellow
    Write-Host "- NIFs: $nifs"
    Write-Host "- Codigos postales: $postalCodes"
    Write-Host "`nOrigen e importaciones previstas" -ForegroundColor Yellow
    foreach ($item in $Plan) {
        $origin = Get-OriginDescription -AppProperties $appProperties -ModeValue $item.Mode -Tipo $item.Tipo
        $tipoNombre = $TipoInfo[$item.Tipo].Nombre
        Write-Host "- $($item.Grupo) / $($item.Mode) / $($item.Tipo): $tipoNombre. Origen: $origin"
        Write-Host "  Ejecutable: $($item.JarPath)" -ForegroundColor DarkGray
    }
    Write-Host "`nConsecuencias" -ForegroundColor Yellow
    Write-Host "- El script cambiara app.tipo_sindicacion antes de cada bloque de importacion."
    Write-Host "- opendata_con_filtros solo continua si hay NIFs o codigos postales definidos."
    Write-Host "- opendata_sin_filtros no limita por NIF ni por codigo postal."
    Write-Host "- La importacion LOCAL exige que no haya datos previos para ese tipo; si los hay, la aplicacion deberia detenerse."
    Write-Host "- La importacion INTERNET es incremental: compara con lo existente y puede insertar o actualizar registros."
    Write-Host "- Si Hibernate esta en modo 'create' o 'create-drop', hay riesgo de recrear tablas y perder datos existentes."
    Write-Host "- Historicos de rechazados persistidos: $persistRejected. Si estuviera en true, podria generar muchos registros."
    Write-Host "- Reintentos HTTP para modo internet: $httpRetries; espera base entre reintentos: $httpDelay ms."
    Write-Host "`nPara continuar escribe I y pulsa Enter. Cualquier otra respuesta cancela la importacion." -ForegroundColor Cyan
    $answer = Read-Host "Confirmacion"
    if ($answer -ne "I") {
        Log "Importacion cancelada por el usuario en la confirmacion previa."
        Write-Host "Importacion cancelada. No se ha ejecutado Java." -ForegroundColor Yellow
        exit 0
    }
    Log "Importacion confirmada por el usuario."
}

Assert-ParameterValues
Assert-Configuration
$plan = Build-ImportPlan
Assert-RequiredFiltersForFilteredRun -Plan $plan

Log "Grupo solicitado: $Grupo"
Log "Modo solicitado: $Mode"
Log "Tipos solicitados: $(if ($TipoSindicacion) { $TipoSindicacion -join ', ' } else { 'por defecto del grupo' })"
Log "DryRun: $DryRun"
Log "ContinueOnError: $ContinueOnError"
Log "CreateSchemaFirstRun: $CreateSchemaFirstRun"
Log "Directorio operativo: $BaseDir"
Log "Ruta de properties: $PropertiesPath"

Write-Host "`n=== Importador OpenData ===" -ForegroundColor Cyan
Write-Host "Grupo: $Grupo | Modo: $Mode | DryRun: $DryRun | CreateSchemaFirstRun: $CreateSchemaFirstRun" -ForegroundColor Cyan
Write-Host "Directorio operativo: $BaseDir" -ForegroundColor DarkCyan
Write-Host "Properties: $PropertiesPath" -ForegroundColor DarkCyan

Show-ImportPlanAndConfirm -Plan $plan

$failed = @()
$executionIndex = 0
foreach ($item in $plan) {
    Log "Preparando importacion: grupo=$($item.Grupo) modo=$($item.Mode) tipo=$($item.Tipo) jar=$($item.JarPath)"
    Write-Host "`n=== Ejecutando $($item.Grupo) / $($item.Mode) / $($item.Tipo) ===" -ForegroundColor Yellow
    Write-Host "JAR: $($item.JarPath)" -ForegroundColor DarkYellow
    $ddlModeForRun = $null
    if ($CreateSchemaFirstRun) {
        $ddlModeForRun = if ($executionIndex -eq 0) { "create" } else { "none" }
    }
    if ($DryRun) {
        Write-Host "DryRun: se usaria app.tipo_sindicacion=$($item.Tipo)" -ForegroundColor Cyan
        if ($ddlModeForRun) {
            Write-Host "DryRun: se usaria hibernate.hbm2ddl.auto=$ddlModeForRun" -ForegroundColor Cyan
        }
        Write-Host "DryRun: & $JavaExe $($JavaOpts -join ' ') -jar `"$($item.JarPath)`" --configDir=`"$PropertiesDir`"" -ForegroundColor Cyan
        Log "DryRun: no se modifica app.properties ni hibernate.properties ni se ejecuta Java."
        $executionIndex++
        continue
    }
    if ($ddlModeForRun) {
        Set-HibernateDdlMode -ModeValue $ddlModeForRun
        Log "hibernate.hbm2ddl.auto actualizado a $ddlModeForRun"
        Write-Host "hibernate.hbm2ddl.auto=$ddlModeForRun" -ForegroundColor DarkCyan
    }
    Set-TipoSindicacion -Tipo $item.Tipo
    Log "app.tipo_sindicacion actualizado a $($item.Tipo)"
    & $JavaExe @JavaOpts -jar $item.JarPath "--configDir=$PropertiesDir"
    $exitCode = $LASTEXITCODE
    $executionIndex++
    if ($exitCode -eq 0) {
        Write-Host "Importacion completada: $($item.Grupo) / $($item.Mode) / $($item.Tipo)" -ForegroundColor Green
        Log "Importacion completada: grupo=$($item.Grupo) modo=$($item.Mode) tipo=$($item.Tipo)"
    } else {
        $message = "ERROR en importacion: grupo=$($item.Grupo) modo=$($item.Mode) tipo=$($item.Tipo) exitCode=$exitCode"
        Write-Host $message -ForegroundColor Red
        Log $message
        $failed += $message
        if (-not $ContinueOnError) {
            exit $exitCode
        }
    }
}

if ($failed.Count -gt 0) {
    Log "Proceso finalizado con errores: $($failed.Count)"
    Write-Host "`n=== Proceso finalizado con errores ===`nLog: $logFile" -ForegroundColor Red
    exit 1
}

Log "Todas las importaciones completadas."
Write-Host "`n=== Proceso finalizado ===`nLog: $logFile" -ForegroundColor Cyan
