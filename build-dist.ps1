param(
    [switch]$SkipTests = $true,
    [string]$DistDir = "dist\FlashCardJava"
)

$ErrorActionPreference = "Stop"

Write-Host "=== Building FlashCard Java ===" -ForegroundColor Cyan

# Clean target with retry if files are locked
if (Test-Path "target") {
    $retries = 3
    do {
        Remove-Item -Recurse -Force "target" -ErrorAction SilentlyContinue
        if (!(Test-Path "target")) { break }
        $retries--
        if ($retries -gt 0) { Start-Sleep -Seconds 2 }
    } while ($retries -gt 0)
}

$mvnArgs = @("clean", "package")
if ($SkipTests) { $mvnArgs += "-DskipTests" }

Write-Host "Running: mvn $($mvnArgs -join ' ')" -ForegroundColor Yellow
$output = & mvn $mvnArgs 2>&1
$output | Out-String | ForEach-Object { Write-Host $_ }
if ($global:LASTEXITCODE -ne 0) { throw "Maven build failed (exit code: $global:LASTEXITCODE)" }

Write-Host "`n=== Preparing distribution ===" -ForegroundColor Cyan
$distPath = Join-Path (Get-Location) $DistDir

if (Test-Path $distPath) { Remove-Item -Recurse -Force $distPath }
New-Item -ItemType Directory -Path $distPath -Force | Out-Null

Write-Host "Copying jlink runtime..." -ForegroundColor Yellow
Copy-Item -Recurse -Path "target\flashCardJava-runtime" -Destination "$distPath\runtime"

if (Test-Path "themes") {
    Write-Host "Copying themes folder..." -ForegroundColor Yellow
    Copy-Item -Recurse -Path "themes" -Destination "$distPath\themes"
}

Copy-Item "target\flashCardJava-1.0-SNAPSHOT.jar" -Destination "$distPath\"

$size = (Get-ChildItem -Recurse $distPath | Measure-Object Length -Sum).Sum
Write-Host "`n=== Distribution ready ===" -ForegroundColor Green
Write-Host "  Path: $distPath"
Write-Host "  Size: $([math]::Round($size/1MB, 1)) MB"
Write-Host "`nQuick test:" -ForegroundColor Yellow
Write-Host "  $distPath\runtime\bin\flashCardJava.bat"
