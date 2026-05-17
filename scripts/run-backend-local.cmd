@echo off
setlocal
set "PROJECT_ROOT=%~dp0.."
set "JAVA_EXE=%PROJECT_ROOT%\.tools\jdk-21.0.11+10\bin\java.exe"
set "BACKEND_DIR=%PROJECT_ROOT%\backend"
set "APP_JAR=%BACKEND_DIR%\target\advanced-warehouse-management-0.0.1-SNAPSHOT.jar"
set "LOG_FILE=%BACKEND_DIR%\backend-local.log"

cd /d "%BACKEND_DIR%"
echo Starting WMS backend at %DATE% %TIME%>> "%LOG_FILE%"
"%JAVA_EXE%" -jar "%APP_JAR%" --spring.profiles.active=local >> "%LOG_FILE%" 2>&1
echo Backend stopped at %DATE% %TIME%>> "%LOG_FILE%"
