#!/usr/bin/env pwsh
# Build and run Hytale server with plugin

# Save current directory
$originalPath = Get-Location

# Configuration
$serverPath = [Environment]::GetFolderPath('Desktop') + "\HytaleServer"
$serverJarPath = "$serverPath\Server\HytaleServer.jar"
$assetsPath = "$serverPath\Assets.zip"

Write-Host "Building plugin..." -ForegroundColor Cyan
./gradlew clean fatJar

if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed!" -ForegroundColor Red
    exit 1
}

Write-Host "Build successful!" -ForegroundColor Green

$jarFile = "build/libs/TaleStatistics-1.0-SNAPSHOT-all.jar"
$modsPath = "$serverPath\mods"

# Create mods directory if it doesn't exist
if (!(Test-Path $modsPath)) {
    Write-Host "Creating mods directory..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Path $modsPath | Out-Null
}

Write-Host "Copying plugin to server..." -ForegroundColor Cyan
Copy-Item $jarFile -Destination $modsPath -Force

Write-Host "Starting Hytale server..." -ForegroundColor Green

if (!(Test-Path $serverJarPath)) {
    Write-Host "Error: HytaleServer.jar not found at: $serverJarPath" -ForegroundColor Red
    Set-Location $originalPath
    exit 1
}

Set-Location $serverPath
java -jar $serverJarPath --assets $assetsPath

# Return to original directory
Set-Location $originalPath
