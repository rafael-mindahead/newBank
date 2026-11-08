@echo off
title FinanceHub - POO Java 21 + Swing
cd /d "%~dp0"
echo Iniciando o FinanceHub Desktop (Java Swing)...
if exist "backend-java\target\financehub-core-0.1.0-SNAPSHOT.jar" (
    java -jar "backend-java\target\financehub-core-0.1.0-SNAPSHOT.jar"
) else (
    powershell -ExecutionPolicy Bypass -File ".\scripts\iniciar-servidor.ps1"
)
pause
