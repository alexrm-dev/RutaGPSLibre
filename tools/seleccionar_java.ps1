# Ruta GPS Libre: selecciona Java 17-23 para Gradle 8.11.1.
# Devuelve solo la carpeta JAVA_HOME en la salida estandar.
$ErrorActionPreference = 'Continue'
$candidates = New-Object 'System.Collections.Generic.List[string]'

function Add-Candidate([string]$dir) {
    if (-not [string]::IsNullOrWhiteSpace($dir) -and -not $script:candidates.Contains($dir)) {
        $script:candidates.Add($dir)
    }
}

$pf = $env:ProgramFiles
$local = $env:LOCALAPPDATA
Add-Candidate (Join-Path $pf 'Android\Android Studio\jbr')
Add-Candidate (Join-Path $pf 'Android\Android Studio\jre')

foreach ($root in @(
    (Join-Path $pf 'Eclipse Adoptium'),
    (Join-Path $pf 'Java'),
    (Join-Path $pf 'Microsoft'),
    (Join-Path $pf 'Zulu'),
    (Join-Path $pf 'BellSoft'),
    (Join-Path $local 'Programs\Eclipse Adoptium'),
    (Join-Path $env:USERPROFILE '.jdks')
)) {
    if (Test-Path -LiteralPath $root -PathType Container) {
        foreach ($folder in @(Get-ChildItem -LiteralPath $root -Directory -ErrorAction SilentlyContinue)) {
            Add-Candidate $folder.FullName
        }
    }
}
Add-Candidate $env:ORIGINAL_JAVA_HOME
$onPath = Get-Command java.exe -ErrorAction SilentlyContinue
if ($onPath) {
    Add-Candidate (Split-Path (Split-Path $onPath.Source -Parent) -Parent)
}

foreach ($dir in $candidates) {
    $java = Join-Path $dir 'bin\java.exe'
    $javac = Join-Path $dir 'bin\javac.exe'
    if (-not (Test-Path -LiteralPath $java -PathType Leaf)) { continue }
    if (-not (Test-Path -LiteralPath $javac -PathType Leaf)) { continue }
    try {
        $start = New-Object System.Diagnostics.ProcessStartInfo
        $start.FileName = $java
        $start.Arguments = '-version'
        $start.UseShellExecute = $false
        $start.RedirectStandardError = $true
        $start.RedirectStandardOutput = $true
        $proc = [System.Diagnostics.Process]::Start($start)
        $versionText = $proc.StandardError.ReadToEnd() + $proc.StandardOutput.ReadToEnd()
        $proc.WaitForExit()
        if ($versionText -match '(?:java|openjdk)\s+version\s+"(\d+)') {
            $major = [int]$Matches[1]
            if ($major -ge 17 -and $major -le 23) {
                [Console]::Out.WriteLine($dir)
                exit 0
            }
        }
    } catch { continue }
}
[Console]::Error.WriteLine('[ERROR] No se encontro JDK 17 a 23 en las ubicaciones habituales.')
[Console]::Error.WriteLine('Instala JDK 17 desde https://adoptium.net/temurin/releases/?version=17')
[Console]::Error.WriteLine('o revisa si Android Studio incluye un JBR compatible.')
exit 1
