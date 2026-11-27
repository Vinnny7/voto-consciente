@echo off
setlocal
cd /d "%~dp0"
if not exist ".env" copy /y ".env.example" ".env" >nul
where java >nul 2>&1
if errorlevel 1 (
    echo Instale o Java 21 e abra este arquivo novamente.
    pause
    exit /b 1
)
java -jar "VotoConsciente.jar"
if errorlevel 1 pause
endlocal
