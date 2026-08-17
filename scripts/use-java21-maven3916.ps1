param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]] $MavenArgs
)

$ErrorActionPreference = 'Stop'

$javaHome = 'C:\java\software\jdk-21.0.11'
$mavenHome = 'C:\java\software\apache-maven-3.9.16'

if (-not (Test-Path -LiteralPath "$javaHome\bin\java.exe")) {
    throw "No se encuentra java.exe en $javaHome\bin"
}

if (-not (Test-Path -LiteralPath "$mavenHome\bin\mvn.cmd")) {
    throw "No se encuentra mvn.cmd en $mavenHome\bin"
}

$env:JAVA_HOME = $javaHome
$env:MAVEN_HOME = $mavenHome
$env:Path = "$javaHome\bin;$mavenHome\bin;$env:Path"

Write-Host "JAVA_HOME=$env:JAVA_HOME"
& java -version
& mvn -version

if ($MavenArgs.Count -gt 0) {
    & mvn @MavenArgs
    exit $LASTEXITCODE
}
