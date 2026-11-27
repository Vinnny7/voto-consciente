@echo off
setlocal
cd /d "%~dp0"
where javac >nul 2>&1
if errorlevel 1 (
    echo Instale o JDK 21 completo para compilar.
    pause
    exit /b 1
)
if exist "classes" rmdir /s /q "classes"
mkdir "classes"
javac -encoding UTF-8 -cp "lib\gson-2.11.0.jar" -d "classes" -sourcepath "src" "src\aplicacao\Main.java"
if errorlevel 1 (
    pause
    exit /b 1
)
pushd "classes"
jar xf "..\lib\gson-2.11.0.jar"
if errorlevel 1 (
    popd
    pause
    exit /b 1
)
if exist "META-INF\MANIFEST.MF" del "META-INF\MANIFEST.MF"
popd
jar --create --file "VotoConsciente.jar" --main-class aplicacao.Main -C "classes" . -C "recursos" .
if errorlevel 1 (
    pause
    exit /b 1
)
echo Compilacao concluida. Abra INICIAR.bat.
pause
endlocal
