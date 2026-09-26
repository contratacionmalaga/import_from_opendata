param(
    [Parameter(Mandatory = $true)]
    [ValidateNotNullOrEmpty()]
    [string]$SourcePropertiesDirectory,

    [Parameter(Mandatory = $true)]
    [ValidateNotNullOrEmpty()]
    [string]$DestinationPropertiesDirectory,

    [Parameter(Mandatory = $true)]
    [ValidateNotNullOrEmpty()]
    [string]$CorpusRoot,

    [ValidateSet("MAYORES", "MENORES", "ENCARGOS", "CONSULTAS", "AGREGADAS")]
    [string]$TipoSindicacion = "MAYORES",

    [ValidateNotNullOrEmpty()]
    [string]$DatabaseName = "opendata_prueba",

    [string]$DatabaseUser,

    [string]$DatabasePassword,

    [ValidateRange(0, 1000)]
    [int]$HibernateBatchSize = 0,

    [ValidateRange(0, 100000)]
    [int]$EntryFlushWindow = 0
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Set-PropertyValue {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)][string]$Key,
        [Parameter(Mandatory = $true)][string]$Value
    )

    $content = Get-Content -LiteralPath $Path
    $escapedKey = [regex]::Escape($Key)
    $replacement = "$Key=$Value"
    $found = $false
    $updated = $content | ForEach-Object {
        if ($_ -match "^\s*$escapedKey\s*=") {
            $found = $true
            $replacement
        }
        else {
            $_
        }
    }

    if (-not $found) {
        $updated += $replacement
    }

    $updated | Set-Content -LiteralPath $Path -Encoding utf8
}

$source = [System.IO.Path]::GetFullPath($SourcePropertiesDirectory)
$destination = [System.IO.Path]::GetFullPath($DestinationPropertiesDirectory)
$corpus = [System.IO.Path]::GetFullPath($CorpusRoot)

if (-not (Test-Path -LiteralPath $source -PathType Container)) {
    throw "No existe el directorio de propiedades fuente: $source"
}
if (-not (Test-Path -LiteralPath $corpus -PathType Container)) {
    throw "No existe el directorio del corpus: $corpus"
}
if (Test-Path -LiteralPath $destination) {
    throw "El directorio de propiedades de F4 ya existe: $destination"
}

[void](New-Item -ItemType Directory -Path $destination)
foreach ($fileName in @("app.properties", "filter.properties", "hibernate.properties", "bd.properties", "mail.properties")) {
    $sourceFile = Join-Path $source $fileName
    if (-not (Test-Path -LiteralPath $sourceFile -PathType Leaf)) {
        throw "Falta el fichero de configuración requerido: $sourceFile"
    }
    Copy-Item -LiteralPath $sourceFile -Destination (Join-Path $destination $fileName)
}

$corpusWithSeparator = ($corpus.TrimEnd([System.IO.Path]::DirectorySeparatorChar, [System.IO.Path]::AltDirectorySeparatorChar) + [System.IO.Path]::DirectorySeparatorChar).Replace('\', '/')
Set-PropertyValue -Path (Join-Path $destination "app.properties") -Key "app.local.path" -Value $corpusWithSeparator
Set-PropertyValue -Path (Join-Path $destination "app.properties") -Key "app.tipo_sindicacion" -Value $TipoSindicacion
Set-PropertyValue -Path (Join-Path $destination "app.properties") -Key "app.email.enabled" -Value "false"
Set-PropertyValue -Path (Join-Path $destination "app.properties") -Key "app.persistir_historicos_rechazados" -Value "false"
Set-PropertyValue -Path (Join-Path $destination "hibernate.properties") -Key "hibernate.hbm2ddl.auto" -Value "validate"
if ($HibernateBatchSize -gt 0) {
    Set-PropertyValue -Path (Join-Path $destination "hibernate.properties") -Key "hibernate.jdbc.batch_size" -Value $HibernateBatchSize
}
if ($EntryFlushWindow -gt 0) {
    Set-PropertyValue -Path (Join-Path $destination "hibernate.properties") -Key "hibernate.persistence.entry_flush_window" -Value $EntryFlushWindow
}

$databasePath = Join-Path $destination "bd.properties"
$databaseContent = Get-Content -LiteralPath $databasePath -Raw
$urlPattern = '(?m)^(jakarta\.persistence\.jdbc\.url=jdbc:(?:mariadb|postgresql)://[^/\r\n]+/)[^?\\\r\n]+'
if ($databaseContent -notmatch $urlPattern) {
    throw "No se ha podido sustituir la base de datos en $databasePath"
}
$databaseContent = [regex]::Replace($databaseContent, $urlPattern, ('$1' + $DatabaseName), 1)
$databaseContent | Set-Content -LiteralPath $databasePath -Encoding utf8

if ($PSBoundParameters.ContainsKey("DatabaseUser")) {
    Set-PropertyValue -Path $databasePath -Key "jakarta.persistence.jdbc.user" -Value $DatabaseUser
}
if ($PSBoundParameters.ContainsKey("DatabasePassword")) {
    Set-PropertyValue -Path $databasePath -Key "jakarta.persistence.jdbc.password" -Value $DatabasePassword
}

$metadata = [PSCustomObject]@{
    createdAt = (Get-Date).ToUniversalTime().ToString("o")
    corpusRoot = $corpus
    tipoSindicacion = $TipoSindicacion
    databaseName = $DatabaseName
    hibernateDdl = "validate"
    hibernateBatchSize = $HibernateBatchSize
    entryFlushWindow = $EntryFlushWindow
    emailEnabled = $false
}
$metadata | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $destination "f4-run-config.json") -Encoding utf8

Write-Host "Configuración F4 preparada: $destination" -ForegroundColor Green
Write-Host "Destino JDBC: base $DatabaseName; credenciales no mostradas."
