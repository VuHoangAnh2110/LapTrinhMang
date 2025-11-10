@echo off
REM Script chi chay FTP Server

echo ===============================================
echo    FTP SERVER
echo ===============================================
echo.

REM Kiem tra JAVA_HOME
if "%JAVA_HOME%" == "" (
    echo [CANH BAO] JAVA_HOME chua duoc thiet lap!
    echo Dang su dung Java tu PATH...
) else (
    echo JAVA_HOME: %JAVA_HOME%
)

echo.
echo Dang compile project...
call mvnw.cmd clean compile -q

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [LOI] Khong the compile project!
    pause
    exit /b 1
)

echo.
echo [OK] Compile thanh cong!
echo.
echo Khoi dong FTP Server tren port 2121...
echo Nhan Ctrl+C de dung server
echo.

REM Chay server
call mvnw.cmd exec:java -Dexec.mainClass="com.example.uploadfileftp.ftpserver.FTPServer"

pause
