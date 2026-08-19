param(
    [string]$OutputDir = "C:\java\ejecutables\import-from-opendata-ejecutables",

    [bool]$SkipTests = $true,

    [switch]$SkipClean
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$repoRoot = $PSScriptRoot
$mavenScript = Join-Path $repoRoot "scripts\use-java21-maven3916.ps1"
$pomPath = Join-Path $repoRoot "pom.xml"
$targetDir = Join-Path $repoRoot "target"

$profiles = @(
    @{ Name = "con-filtros-local"; Artifact = "opendata_con_filtros"; Mode = "local" },
    @{ Name = "con-filtros-internet"; Artifact = "opendata_con_filtros"; Mode = "internet" },
    @{ Name = "sin-filtros-local"; Artifact = "opendata_sin_filtros"; Mode = "local" },
    @{ Name = "sin-filtros-internet"; Artifact = "opendata_sin_filtros"; Mode = "internet" }
)

if (-not (Test-Path -LiteralPath $mavenScript)) {
    throw "No se encuentra el script Maven del proyecto: $mavenScript"
}

if (-not (Test-Path -LiteralPath $pomPath)) {
    throw "No se encuentra pom.xml en $pomPath"
}

[xml]$pom = Get-Content -Raw -LiteralPath $pomPath
$projectVersion = [string]$pom.project.version
if ([string]::IsNullOrWhiteSpace($projectVersion)) {
    throw "No se ha podido resolver la version del proyecto desde $pomPath"
}

if (-not (Test-Path -LiteralPath $OutputDir)) {
    New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null
}

Push-Location $repoRoot
try {
    foreach ($profile in $profiles) {
        Write-Host "Compilando perfil $($profile.Name)" -ForegroundColor Cyan

        $mavenArgs = @()
        if (-not $SkipClean) {
            $mavenArgs += "clean"
        }
        $mavenArgs += "package"
        $mavenArgs += "-P$($profile.Name)"
        if ($SkipTests) {
            $mavenArgs += "-DskipTests"
        }

        & $mavenScript @mavenArgs
        if ($LASTEXITCODE -ne 0) {
            throw "Fallo Maven para el perfil $($profile.Name) con codigo $LASTEXITCODE"
        }

        $jarName = "$($profile.Artifact)-$projectVersion-$($profile.Mode).jar"
        $jarPath = Join-Path $targetDir $jarName
        if (-not (Test-Path -LiteralPath $jarPath)) {
            throw "No se ha encontrado el JAR esperado para el perfil $($profile.Name): $jarPath"
        }

        $destination = Join-Path $OutputDir $jarName
        Copy-Item -LiteralPath $jarPath -Destination $destination -Force
        Write-Host "Copiado $jarName -> $OutputDir" -ForegroundColor Green
    }
}
finally {
    Pop-Location
}

Write-Host "Ejecutables generados en $OutputDir" -ForegroundColor Green

