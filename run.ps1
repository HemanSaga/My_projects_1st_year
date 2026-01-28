# Inventory Management System - Run Script
# This script sets up Java 24 and runs the application

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Inventory Management System" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Set Java 24
$env:JAVA_HOME = "C:\Program Files\Java\jdk-24"
$env:Path = "C:\Program Files\Java\jdk-24\bin;" + $env:Path

Write-Host "✓ Java 24 configured" -ForegroundColor Green
java -version
Write-Host ""

# Check MySQL
Write-Host "Checking MySQL status..." -ForegroundColor Yellow
$mysqlService = Get-Service -Name MySQL* -ErrorAction SilentlyContinue
if ($mysqlService -and $mysqlService.Status -eq "Running") {
    Write-Host "✓ MySQL is running" -ForegroundColor Green
} else {
    Write-Host "✗ MySQL is not running!" -ForegroundColor Red
    Write-Host "  Starting MySQL..." -ForegroundColor Yellow
    net start MySQL80
}
Write-Host ""

# Run application using Maven
Write-Host "Starting application..." -ForegroundColor Yellow
Write-Host ""

mvn javafx:run
