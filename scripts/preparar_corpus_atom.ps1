param(
    [Parameter(Mandatory = $true)]
    [ValidateNotNullOrEmpty()]
    [string]$SourceDirectory,

    [Parameter(Mandatory = $true)]
    [ValidateNotNullOrEmpty()]
    [string]$DestinationDirectory,

    [Parameter(Mandatory = $true)]
    [ValidateNotNullOrEmpty()]
    [string]$InitialFile,

    [ValidateRange(0, [int]::MaxValue)]
    [int]$MaxEntries = 0
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Get-CanonicalPath {
    param([Parameter(Mandatory = $true)][string]$Path)

    return [System.IO.Path]::GetFullPath($Path)
}

function Resolve-FeedPath {
    param(
        [Parameter(Mandatory = $true)][string]$BaseDirectory,
        [Parameter(Mandatory = $true)][string]$RelativeFile
    )

    $candidate = Get-CanonicalPath -Path (Join-Path $BaseDirectory $RelativeFile)
    $baseWithSeparator = $BaseDirectory.TrimEnd([System.IO.Path]::DirectorySeparatorChar, [System.IO.Path]::AltDirectorySeparatorChar) + [System.IO.Path]::DirectorySeparatorChar

    if (-not $candidate.StartsWith($baseWithSeparator, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "El enlace ATOM apunta fuera del directorio del corpus: $RelativeFile"
    }

    if (-not (Test-Path -LiteralPath $candidate -PathType Leaf)) {
        throw "No existe el fichero ATOM indicado por next: $candidate"
    }

    return $candidate
}

function Save-AtomWithoutNext {
    param(
        [Parameter(Mandatory = $true)][System.Xml.XmlDocument]$Document,
        [Parameter(Mandatory = $true)][System.Xml.XmlNode]$NextLink,
        [Parameter(Mandatory = $true)][string]$Path
    )

    [void]$NextLink.ParentNode.RemoveChild($NextLink)
    $settings = [System.Xml.XmlWriterSettings]::new()
    $settings.Encoding = [System.Text.UTF8Encoding]::new($false)
    $settings.Indent = $false

    $writer = [System.Xml.XmlWriter]::Create($Path, $settings)
    try {
        $Document.Save($writer)
    }
    finally {
        $writer.Dispose()
    }
}

$sourceRoot = Get-CanonicalPath -Path $SourceDirectory
$destinationRoot = Get-CanonicalPath -Path $DestinationDirectory

if (-not (Test-Path -LiteralPath $sourceRoot -PathType Container)) {
    throw "No existe el directorio fuente: $sourceRoot"
}

if (Test-Path -LiteralPath $destinationRoot) {
    throw "El directorio destino ya existe. Use un directorio nuevo para no mezclar corpus: $destinationRoot"
}

[void](New-Item -ItemType Directory -Path $destinationRoot)

$currentFile = Resolve-FeedPath -BaseDirectory $sourceRoot -RelativeFile $InitialFile
$visitedFiles = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::OrdinalIgnoreCase)
$pages = [System.Collections.Generic.List[object]]::new()
$totalEntries = 0L
$totalDeletedEntries = 0L
$cutApplied = $false

while ($true) {
    if (-not $visitedFiles.Add($currentFile)) {
        throw "Ciclo detectado al preparar el corpus: $currentFile"
    }

    $document = [System.Xml.XmlDocument]::new()
    $document.PreserveWhitespace = $true
    $document.Load($currentFile)

    $entryCount = [long]$document.SelectNodes("/*[local-name()='feed']/*[local-name()='entry']").Count
    $deletedEntryCount = [long]$document.SelectNodes("/*[local-name()='feed']/*[local-name()='deleted-entry']").Count
    $nextLink = $document.SelectSingleNode("/*[local-name()='feed']/*[local-name()='link'][@rel='next']")
    $relativeOutput = [System.IO.Path]::GetRelativePath($sourceRoot, $currentFile)
    $outputFile = Get-CanonicalPath -Path (Join-Path $destinationRoot $relativeOutput)
    $outputDirectory = Split-Path -Parent $outputFile
    [void](New-Item -ItemType Directory -Force -Path $outputDirectory)

    $totalEntries += $entryCount
    $totalDeletedEntries += $deletedEntryCount
    $cutHere = $MaxEntries -gt 0 -and $totalEntries -ge $MaxEntries

    if ($cutHere -and $null -ne $nextLink) {
        Save-AtomWithoutNext -Document $document -NextLink $nextLink -Path $outputFile
        $cutApplied = $true
    }
    else {
        Copy-Item -LiteralPath $currentFile -Destination $outputFile
    }

    $pages.Add([PSCustomObject]@{
            file = $relativeOutput
            entries = $entryCount
            deletedEntries = $deletedEntryCount
            terminalPage = $cutHere -or $null -eq $nextLink
        })

    if ($cutHere -or $null -eq $nextLink) {
        break
    }

    $nextFile = $nextLink.GetAttribute("href")
    if ([string]::IsNullOrWhiteSpace($nextFile)) {
        throw "El enlace next no contiene href en $currentFile"
    }

    $currentFile = Resolve-FeedPath -BaseDirectory $sourceRoot -RelativeFile $nextFile
}

$manifest = [PSCustomObject]@{
    createdAt = (Get-Date).ToUniversalTime().ToString("o")
    sourceDirectory = $sourceRoot
    initialFile = $InitialFile
    requestedMaxEntries = $MaxEntries
    copiedPages = $pages.Count
    copiedEntries = $totalEntries
    copiedDeletedEntries = $totalDeletedEntries
    nextLinkRemoved = $cutApplied
    pages = $pages
}

$manifest | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath (Join-Path $destinationRoot "manifest.json") -Encoding utf8

Write-Host "Corpus preparado: paginas=$($pages.Count), entries=$totalEntries, bajas=$totalDeletedEntries" -ForegroundColor Green
if ($MaxEntries -gt 0 -and $totalEntries -gt $MaxEntries) {
    Write-Host "El limite se ha superado para conservar la pagina ATOM completa." -ForegroundColor Yellow
}
Write-Host "Manifiesto: $(Join-Path $destinationRoot 'manifest.json')"
