@echo off
REM Script to start JSON Server for testing Music File Manager API
REM Author: Music File Manager Team

echo ==========================================
echo  Music File Manager - JSON Server
echo ==========================================
echo.

REM Check if node is installed
where node >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Node.js not found!
    echo Please install Node.js from: https://nodejs.org/
    pause
    exit /b 1
)

REM Check if json-server is installed
where json-server >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [INFO] JSON Server not found. Installing...
    npm install -g json-server
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] Failed to install JSON Server
        pause
        exit /b 1
    )
)

echo [OK] JSON Server is ready
echo.
echo ==========================================
echo  Starting JSON Server...
echo ==========================================
echo.
echo API Base URL: http://localhost:8080
echo API Endpoints:
echo   - GET    http://localhost:8080/genres
echo   - GET    http://localhost:8080/genres/:id
echo   - POST   http://localhost:8080/genres
echo   - PUT    http://localhost:8080/genres/:id
echo   - DELETE http://localhost:8080/genres/:id
echo.
echo For Android Emulator, use: http://10.0.2.2:8080/api/
echo.
echo Press Ctrl+C to stop server
echo ==========================================
echo.

REM Start JSON Server
json-server --watch db.json --port 8080 --routes routes.json

pause

