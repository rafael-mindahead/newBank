# Script para compilar e abrir a Interface Gráfica Desktop Java Swing do FinanceHub
$ErrorActionPreference = "Stop"

$javaCmd = Get-Command java -ErrorAction SilentlyContinue
if (-not $javaCmd) {
    Write-Error "Java 21 nao encontrado no PATH."
    exit 1
}

$env:JAVA_HOME = Split-Path (Split-Path $javaCmd.Source -Parent) -Parent
$rootDir = Split-Path $PSScriptRoot -Parent
$backendDir = Join-Path $rootDir "backend-java"
$outDir = Join-Path $backendDir "target\classes"

New-Item -ItemType Directory -Force -Path $outDir | Out-Null

Write-Host "Compilando classes Java 21 + Swing do FinanceHub..." -ForegroundColor Cyan
$javaFiles = Get-ChildItem -Path (Join-Path $backendDir "src\main\java") -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }
& javac -encoding UTF-8 -d $outDir $javaFiles

Write-Host "Abrindo janela desktop Java Swing do FinanceHub..." -ForegroundColor Green
Set-Location $rootDir
& java -cp $outDir br.com.financehub.app.FinanceHubApplication
