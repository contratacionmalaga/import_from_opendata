param(
    [Parameter(Mandatory = $true)]
    [ValidateNotNullOrEmpty()]
    [string]$JavaPath,

    [Parameter(Mandatory = $true)]
    [ValidateNotNullOrEmpty()]
    [string]$JarPath,

    [Parameter(Mandatory = $true)]
    [ValidateNotNullOrEmpty()]
    [string]$ConfigDirectory,

    [Parameter(Mandatory = $true)]
    [ValidateNotNullOrEmpty()]
    [string]$OutputDirectory,

    [ValidateRange(256, 16384)]
    [int]$HeapMiB = 2048
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

foreach ($path in @($JavaPath, $JarPath, $ConfigDirectory)) {
    if (-not (Test-Path -LiteralPath $path)) {
        throw "No existe la ruta requerida: $path"
    }
}

if (Test-Path -LiteralPath $OutputDirectory) {
    throw "El directorio de salida ya existe: $OutputDirectory"
}

[void](New-Item -ItemType Directory -Path $OutputDirectory)
$absoluteOutput = [System.IO.Path]::GetFullPath($OutputDirectory)
$jfrPath = Join-Path $absoluteOutput "importacion.jfr"
$stdoutPath = Join-Path $absoluteOutput "importacion.stdout.log"
$stderrPath = Join-Path $absoluteOutput "importacion.stderr.log"
$resultPath = Join-Path $absoluteOutput "resultado.json"
$absoluteConfig = [System.IO.Path]::GetFullPath($ConfigDirectory)

$arguments = @(
    "-Xms512m",
    "-Xmx$($HeapMiB)m",
    "-XX:StartFlightRecording=filename=$jfrPath,settings=profile,dumponexit=true",
    "-jar",
    [System.IO.Path]::GetFullPath($JarPath),
    "--configDir=$absoluteConfig"
)

$startedAt = Get-Date
$process = Start-Process -FilePath $JavaPath -ArgumentList $arguments -RedirectStandardOutput $stdoutPath -RedirectStandardError $stderrPath -PassThru -Wait -NoNewWindow
$finishedAt = Get-Date

$result = [PSCustomObject]@{
    startedAtUtc = $startedAt.ToUniversalTime().ToString("o")
    finishedAtUtc = $finishedAt.ToUniversalTime().ToString("o")
    elapsedSeconds = [Math]::Round(($finishedAt - $startedAt).TotalSeconds, 3)
    exitCode = $process.ExitCode
    heapMiB = $HeapMiB
    jfrPath = $jfrPath
    stdoutPath = $stdoutPath
    stderrPath = $stderrPath
}
$result | ConvertTo-Json | Set-Content -LiteralPath $resultPath -Encoding utf8

Write-Host "Perfil JFR finalizado: exitCode=$($process.ExitCode), segundos=$($result.elapsedSeconds)"
if ($process.ExitCode -ne 0) {
    exit $process.ExitCode
}
