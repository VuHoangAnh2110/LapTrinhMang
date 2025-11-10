@echo off
REM Script khoi dong FTP Server va Client

echo ===============================================
echo    FTP CLIENT-SERVER APPLICATION
echo ===============================================
echo.

REM Kiem tra JAVA_HOME
if "%JAVA_HOME%" == "" (
    echo [CANH BAO] JAVA_HOME chua duoc thiet lap!
    echo Dang su dung Java tu PATH...
    set JAVA_CMD=java
) else (
    echo JAVA_HOME: %JAVA_HOME%
    set JAVA_CMD="%JAVA_HOME%\bin\java"
)

echo.
echo Dang compile project...
call mvnw.cmd clean compile -q

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [LOI] Khong the compile project!
    echo Vui long kiem tra lai code hoac cau hinh Maven.
    pause
    exit /b 1
)

echo.
echo [OK] Compile thanh cong!
echo.
echo Khoi dong Server va Client...
echo Server se chay tren port 2121
echo.

REM Chay ung dung
call mvnw.cmd javafx:run

pause
