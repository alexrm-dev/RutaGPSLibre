@echo off
chcp 65001 >nul
setlocal EnableExtensions
cd /d "%~dp0"
echo ========================================
echo   RUTA GPS LIBRE - GENERADOR DE APK
echo          Version v0.2.2
echo ========================================
echo.

rem Evita la seleccion accidental de Java 25 mediante JAVA_HOME.
set "ORIGINAL_JAVA_HOME=%JAVA_HOME%"
set "JAVA_HOME="
for /f "usebackq delims=" %%J in (`powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0tools\seleccionar_java.ps1"`) do set "JAVA_HOME=%%J"
if not defined JAVA_HOME (
  echo.
  echo [ERROR] Es necesario un JDK compatible: 17, 18, 19, 20, 21, 22 o 23.
  echo Instala JDK 17: https://adoptium.net/temurin/releases/?version=17
  echo Despues vuelve a ejecutar este mismo archivo.
  goto :failed
)
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo Java elegido: %JAVA_HOME%
"%JAVA_HOME%\bin\java.exe" -version
echo.

if not defined ANDROID_HOME set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
if not exist "%ANDROID_HOME%\platforms\android-35\android.jar" (
  echo [ERROR] No encuentro Android SDK 35.
  echo Instala Android Studio y en SDK Manager instala "Android API 35".
  echo Ubicacion comprobada: %ANDROID_HOME%
  goto :failed
)
set "GRADLE=.herramientas\gradle-8.11.1\bin\gradle.bat"
if not exist "%GRADLE%" (
  echo Descargando Gradle 8.11.1 desde el sitio oficial ...
  if not exist ".herramientas" mkdir ".herramientas"
  powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $ProgressPreference='SilentlyContinue'; Invoke-WebRequest 'https://services.gradle.org/distributions/gradle-8.11.1-bin.zip' -OutFile '.herramientas\gradle.zip'; Expand-Archive '.herramientas\gradle.zip' -DestinationPath '.herramientas' -Force; Remove-Item '.herramientas\gradle.zip'"
  if errorlevel 1 (
    echo [ERROR] Fallo la descarga de Gradle. Comprueba internet.
    goto :failed
  )
)
echo.
echo Compilando codigo y descargando dependencias...
call "%GRADLE%" --no-daemon :app:testDebugUnitTest :app:assembleDebug
if errorlevel 1 goto :failed
echo.
echo ========================================
echo APK GENERADO:
echo %CD%\app\build\outputs\apk\debug\app-debug.apk
echo ========================================
echo Instala SOLO en tu telefono de pruebas.
pause
exit /b 0
:failed
echo.
echo No se genero el APK. Copia el nuevo error si aparece.
pause
exit /b 1
