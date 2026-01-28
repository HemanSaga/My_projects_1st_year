@echo off
echo ========================================
echo  Inventory Management System
echo ========================================
echo.

REM using system java (verified as Java 24)
java -version
echo.

echo Starting application...
echo.

java -jar target\inventory-management-system-1.0.0.jar

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Application exited with error code %ERRORLEVEL%
    pause
)
