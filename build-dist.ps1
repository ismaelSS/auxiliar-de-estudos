param(
    [switch]$SkipTests = $true,
    [string]$OutputDir = "dist\installer"
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
& mvn $mvnArgs
if ($LASTEXITCODE -ne 0) { throw "Maven build failed (exit code: $LASTEXITCODE)" }

Write-Host "`n=== Creating EXE with jpackage ===" -ForegroundColor Cyan
$runtimeImage = Join-Path (Get-Location) "target\flashCardJava-runtime"
$iconPath = Join-Path (Get-Location) "src\main\resources\icon.ico"
$outputPath = Join-Path (Get-Location) $OutputDir

if (Test-Path $outputPath) { Remove-Item -Recurse -Force $outputPath }

jpackage --type app-image `
    --runtime-image $runtimeImage `
    --module "org.IsmaelSS.flashCardJava/org.IsmaelSS.Main" `
    --name "FlashCardJava" `
    --app-version "1.0" `
    --icon $iconPath `
    --dest $outputPath `
    --vendor "IsmaelSS"

if ($global:LASTEXITCODE -ne 0) { throw "jpackage failed (exit code: $global:LASTEXITCODE)" }

Write-Host "`n=== Copying themes folder ===" -ForegroundColor Cyan
$appDir = Join-Path $outputPath "FlashCardJava"
Copy-Item -Recurse -Path "themes" -Destination "$appDir\themes"

$exePath = Join-Path $appDir "FlashCardJava.exe"
$size = (Get-ChildItem -Recurse $appDir | Measure-Object Length -Sum).Sum
Write-Host "`n=== Distribution ready ===" -ForegroundColor Green
Write-Host "  EXE: $exePath"
Write-Host "  Size: $([math]::Round($size/1MB, 1)) MB"
Write-Host "`nRun:" -ForegroundColor Yellow
Write-Host "  $exePath"
