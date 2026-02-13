@echo off
REM ============================================================
REM Test Script for MySQL and Oracle Database Connections
REM Windows Batch Script
REM ============================================================

setlocal enabledelayedexpansion

REM Colors (Windows 10+)
set "GREEN=[92m"
set "RED=[91m"
set "YELLOW=[93m"
set "RESET=[0m"

REM Configuration
set "BASE_URL=http://localhost:8080"
set "TIMEOUT=5"

echo.
echo ============================================================
echo  Database Connection Test Script
echo ============================================================
echo.

REM Check if curl is available
where curl >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] curl not found. Please install curl or use PowerShell instead.
    echo.
    echo Alternative: Run test_database.ps1 with PowerShell
    exit /b 1
)

REM Function to test connectivity
echo Testing backend connectivity...
curl -s -m %TIMEOUT% "%BASE_URL%/api/database/supported-types" >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Cannot reach backend at %BASE_URL%
    echo Please ensure the backend is running:
    echo   mvn spring-boot:run
    exit /b 1
)
echo [OK] Backend is running
echo.

REM Test 1: Check supported database types
echo [TEST 1] Getting supported database types...
echo.
curl -s "%BASE_URL%/api/database/supported-types" | findstr /C:"mysql" >nul
if %ERRORLEVEL% EQU 0 (
    echo [PASS] MySQL support detected
) else (
    echo [FAIL] MySQL support not detected
)

curl -s "%BASE_URL%/api/database/supported-types" | findstr /C:"oracle" >nul
if %ERRORLEVEL% EQU 0 (
    echo [PASS] Oracle support detected
) else (
    echo [FAIL] Oracle support not detected
)
echo.

REM Test 2: Check current status
echo [TEST 2] Checking current database status...
echo.
curl -s "%BASE_URL%/api/database/status"
echo.
echo.

REM Test 3: Connect to MySQL
echo [TEST 3] Attempting MySQL connection...
echo.
echo URL: %BASE_URL%/api/database/connect/mysql
echo Parameters:
echo   host: localhost
echo   port: 3306
echo   database: roadmap_mvp
echo   user: root
echo   password: (empty)
echo.
curl -s "%BASE_URL%/api/database/connect/mysql?host=localhost&port=3306&database=roadmap_mvp&user=root&password=" | findstr /C:"SUCCESS" >nul
if %ERRORLEVEL% EQU 0 (
    echo [PASS] MySQL connection attempt sent
    curl -s "%BASE_URL%/api/database/status" | findstr /C:"MYSQL" >nul
    if %ERRORLEVEL% EQU 0 (
        echo [PASS] MySQL connection SUCCESS
    ) else (
        echo [WARN] MySQL connection result unclear
    )
) else (
    echo [WARN] MySQL connection may have failed (check logs)
)
echo.

REM Test 4: Connect to Oracle (with example values)
echo [TEST 4] Attempting Oracle connection (with example parameters)...
echo.
echo URL: %BASE_URL%/api/database/connect/oracle
echo Parameters:
echo   host: oracle-server.example.com
echo   port: 1521
echo   sid: ORCL
echo   user: system
echo   password: oracle
echo.
echo NOTE: This will likely fail unless you have Oracle running at oracle-server.example.com
echo       Modify the host, port, sid, user, and password values below to test your environment.
echo.
set "ORACLE_HOST=oracle-server.example.com"
set "ORACLE_PORT=1521"
set "ORACLE_SID=ORCL"
set "ORACLE_USER=system"
set "ORACLE_PASSWORD=oracle"

curl -s "%BASE_URL%/api/database/connect/oracle?host=!ORACLE_HOST!&port=!ORACLE_PORT!&sid=!ORACLE_SID!&user=!ORACLE_USER!&password=!ORACLE_PASSWORD!" | findstr /C:"ERROR\|FAILED" >nul
if %ERRORLEVEL% NEQ 0 (
    echo [INFO] Oracle connection test (expected to fail with example values)
    curl -s "%BASE_URL%/api/database/connect/oracle?host=!ORACLE_HOST!&port=!ORACLE_PORT!&sid=!ORACLE_SID!&user=!ORACLE_USER!&password=!ORACLE_PASSWORD!"
) else (
    echo [WARN] Oracle connection failed (as expected with example parameters)
)
echo.
echo.

REM Test 5: Check status after operations
echo [TEST 5] Final status check...
echo.
curl -s "%BASE_URL%/api/database/status"
echo.
echo.

REM Summary
echo ============================================================
echo  Test Summary
echo ============================================================
echo.
echo [DONE] Tests completed. Check output above for results.
echo.
echo Notes:
echo   - If MySQL connected: Try creating a roadmap via /api/roadmaps
echo   - If Oracle failed: Update ORACLE_HOST, ORACLE_PORT, ORACLE_SID in script
echo   - For more tests: Use test_database.ps1 (PowerShell version)
echo.
echo ============================================================
echo.

endlocal
