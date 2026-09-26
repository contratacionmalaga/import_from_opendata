[CmdletBinding(SupportsShouldProcess)]
param(
    [string]$SourceDir = $PSScriptRoot,
    [string]$OutputDir = "C:\java\ejecutables\import-from-opendata-ejecutables",
    [string]$Version,
    [string]$RollbackVersion
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$jarDefinitions = @(
    @{ Artifact = "opendata_con_filtros"; Mode = "local" },
    @{ Artifact = "opendata_con_filtros"; Mode = "internet" },
    @{ Artifact = "opendata_sin_filtros"; Mode = "local" },
    @{ Artifact = "opendata_sin_filtros"; Mode = "internet" }
)

function Get-ProjectVersion {
    param([string]$ProjectRoot)
    $pomPath = Join-Path $ProjectRoot "pom.xml"
    if (-not (Test-Path -LiteralPath $pomPath)) { throw "No se encuentra pom.xml: $pomPath" }
    [xml]$pom = Get-Content -Raw -LiteralPath $pomPath
    $value = [string]$pom.project.version
    if ([string]::IsNullOrWhiteSpace($value)) { throw "No se ha podido resolver la version desde $pomPath" }
    return $value.Trim()
}

function Assert-Version {
    param([string]$Value)
    if ($Value -notmatch '^\d+\.\d+\.\d+$') { throw "La version '$Value' debe usar el formato mayor.menor.parche." }
}

function Copy-DirectoryContents {
    param([string]$Source, [string]$Destination)
    if (-not (Test-Path -LiteralPath $Source)) { throw "No se encuentra el directorio de origen: $Source" }
    New-Item -ItemType Directory -Force -Path $Destination | Out-Null
    Get-ChildItem -LiteralPath $Source -Force | ForEach-Object {
        Copy-Item -LiteralPath $_.FullName -Destination $Destination -Recurse -Force
    }
}

function Get-ReleaseFiles {
    param([string]$ProjectRoot, [string]$ReleaseVersion)
    $targetDir = Join-Path $ProjectRoot "target"
    $result = New-Object System.Collections.Generic.List[System.IO.FileInfo]
    foreach ($definition in $jarDefinitions) {
        $name = "$($definition.Artifact)-$ReleaseVersion-$($definition.Mode).jar"
        $path = Join-Path $targetDir $name
        if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "No se encuentra el JAR requerido para publicar: $path" }
        $result.Add((Get-Item -LiteralPath $path))
    }
    return $result
}

function Write-ReleaseManifest {
    param([string]$ReleaseDirectory, [string]$ReleaseVersion, [string]$ProjectRoot)
    $commit = "no disponible"
    try { $commit = (& git -C $ProjectRoot rev-parse HEAD 2>$null).Trim() } catch { }
    $files = Get-ChildItem -LiteralPath $ReleaseDirectory -File -Recurse |
        Sort-Object FullName |
        ForEach-Object {
            [PSCustomObject]@{
                path = $_.FullName.Substring($ReleaseDirectory.Length).TrimStart('\', '/')
                sha256 = (Get-FileHash -Algorithm SHA256 -LiteralPath $_.FullName).Hash
                bytes = $_.Length
            }
        }
    [ordered]@{
        version = $ReleaseVersion
        publishedAtUtc = (Get-Date).ToUniversalTime().ToString("o")
        sourceDirectory = $ProjectRoot
        gitCommit = $commit
        files = @($files)
    } | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath (Join-Path $ReleaseDirectory "manifest.json") -Encoding UTF8
}

function Backup-ActiveProperties {
    param([string]$BaseDirectory)
    $propertiesDir = Join-Path $BaseDirectory "properties"
    if (-not (Test-Path -LiteralPath $propertiesDir)) { return }
    $backupDir = Join-Path $BaseDirectory ("backups\properties\" + (Get-Date -Format "yyyyMMdd_HHmmss"))
    Copy-DirectoryContents -Source $propertiesDir -Destination $backupDir
    Write-Host "Copia de properties anterior: $backupDir" -ForegroundColor DarkYellow
}

function Set-ActiveRelease {
    param([string]$BaseDirectory, [string]$ReleaseDirectory, [string]$ReleaseVersion)
    Backup-ActiveProperties -BaseDirectory $BaseDirectory
    Copy-DirectoryContents -Source (Join-Path $ReleaseDirectory "properties") -Destination (Join-Path $BaseDirectory "properties")
    Get-ChildItem -LiteralPath $ReleaseDirectory -Filter "*.jar" -File | ForEach-Object {
        Copy-Item -LiteralPath $_.FullName -Destination (Join-Path $BaseDirectory $_.Name) -Force
    }
    foreach ($scriptName in @("importar_atoms.ps1", "importar_atom.sh", "publicar_ejecutables.ps1")) {
        $scriptPath = Join-Path $ReleaseDirectory $scriptName
        if (Test-Path -LiteralPath $scriptPath) { Copy-Item -LiteralPath $scriptPath -Destination (Join-Path $BaseDirectory $scriptName) -Force }
    }
    @("version=$ReleaseVersion", "activated_at_utc=$((Get-Date).ToUniversalTime().ToString('o'))") |
        Set-Content -LiteralPath (Join-Path $BaseDirectory "active-release.properties") -Encoding UTF8
}

function Publish-Release {
    param([string]$ProjectRoot, [string]$BaseDirectory, [string]$ReleaseVersion)
    $propertiesSource = Join-Path $ProjectRoot "properties"
    if (-not (Test-Path -LiteralPath $propertiesSource)) { throw "No se encuentra el directorio properties de desarrollo: $propertiesSource" }
    $scriptSources = @("importar_atoms.ps1", "importar_atom.sh", "publicar_ejecutables.ps1")
    foreach ($scriptName in $scriptSources) {
        if (-not (Test-Path -LiteralPath (Join-Path $ProjectRoot $scriptName))) { throw "No se encuentra el script requerido: $scriptName" }
    }
    $versionsDirectory = Join-Path $BaseDirectory "versions"
    $releaseDirectory = Join-Path $versionsDirectory $ReleaseVersion
    if ($WhatIfPreference) {
        Write-Host "WhatIf: se publicaria la version $ReleaseVersion en $releaseDirectory y se sincronizarian los properties activos." -ForegroundColor Cyan
        return
    }
    $jars = Get-ReleaseFiles -ProjectRoot $ProjectRoot -ReleaseVersion $ReleaseVersion
    New-Item -ItemType Directory -Force -Path $versionsDirectory | Out-Null
    $temporaryDirectory = Join-Path $versionsDirectory (".$ReleaseVersion.staging-" + [guid]::NewGuid().ToString("N"))
    New-Item -ItemType Directory -Force -Path $temporaryDirectory | Out-Null
    try {
        foreach ($jar in $jars) { Copy-Item -LiteralPath $jar.FullName -Destination (Join-Path $temporaryDirectory $jar.Name) -Force }
        foreach ($scriptName in $scriptSources) { Copy-Item -LiteralPath (Join-Path $ProjectRoot $scriptName) -Destination (Join-Path $temporaryDirectory $scriptName) -Force }
        Copy-DirectoryContents -Source $propertiesSource -Destination (Join-Path $temporaryDirectory "properties")
        $releaseNotes = Join-Path $ProjectRoot "docs\releases\v$ReleaseVersion.md"
        if (Test-Path -LiteralPath $releaseNotes) { Copy-Item -LiteralPath $releaseNotes -Destination (Join-Path $temporaryDirectory "release-notes.md") -Force }
        $migrationDirectory = Join-Path $ProjectRoot "docs\migrations\v$ReleaseVersion"
        if (Test-Path -LiteralPath $migrationDirectory) { Copy-DirectoryContents -Source $migrationDirectory -Destination (Join-Path $temporaryDirectory "migrations") }
        Write-ReleaseManifest -ReleaseDirectory $temporaryDirectory -ReleaseVersion $ReleaseVersion -ProjectRoot $ProjectRoot
        if (Test-Path -LiteralPath $releaseDirectory) { Remove-Item -LiteralPath $releaseDirectory -Recurse -Force }
        Move-Item -LiteralPath $temporaryDirectory -Destination $releaseDirectory
        Set-ActiveRelease -BaseDirectory $BaseDirectory -ReleaseDirectory $releaseDirectory -ReleaseVersion $ReleaseVersion
    }
    finally {
        if (Test-Path -LiteralPath $temporaryDirectory) { Remove-Item -LiteralPath $temporaryDirectory -Recurse -Force }
    }
    Write-Host "Version $ReleaseVersion publicada y activada en $BaseDirectory" -ForegroundColor Green
}

function Restore-Release {
    param([string]$BaseDirectory, [string]$ReleaseVersion)
    $releaseDirectory = Join-Path $BaseDirectory "versions\$ReleaseVersion"
    if (-not (Test-Path -LiteralPath $releaseDirectory)) { throw "No existe una copia publicada de la version $ReleaseVersion en $releaseDirectory" }
    if ($WhatIfPreference) { Write-Host "WhatIf: se activaria la version $ReleaseVersion desde $releaseDirectory" -ForegroundColor Cyan; return }
    Set-ActiveRelease -BaseDirectory $BaseDirectory -ReleaseDirectory $releaseDirectory -ReleaseVersion $ReleaseVersion
    Write-Host "Version $ReleaseVersion restaurada y activada." -ForegroundColor Green
}

$SourceDir = (Resolve-Path -LiteralPath $SourceDir).Path
$resolvedVersion = if ($RollbackVersion) { $RollbackVersion } elseif ($Version) { $Version } else { Get-ProjectVersion -ProjectRoot $SourceDir }
Assert-Version -Value $resolvedVersion
New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null
if ($RollbackVersion) { Restore-Release -BaseDirectory $OutputDir -ReleaseVersion $RollbackVersion }
else { Publish-Release -ProjectRoot $SourceDir -BaseDirectory $OutputDir -ReleaseVersion $resolvedVersion }
