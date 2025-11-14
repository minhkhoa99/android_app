@echo off
echo =========================================
echo Starting Music Management API Server
echo Port: 3005
echo Database: music_db.json
echo =========================================
echo.

REM Check if json-server is installed
where json-server >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] json-server not found!
    echo Please install it first: npm install -g json-server
    pause
    exit /b 1
)

REM Check if music_db.json exists
if not exist music_db.json (
    echo [ERROR] music_db.json not found!
    echo Please make sure music_db.json exists in the current directory
    pause
    exit /b 1
)

REM Check if music_routes.json exists
if not exist music_routes.json (
    echo [ERROR] music_routes.json not found!
    echo Please make sure music_routes.json exists in the current directory
    pause
    exit /b 1
)

echo [OK] Starting server...
echo.
echo API Endpoints:
echo   GET    http://localhost:3005/api/music-files
echo   GET    http://localhost:3005/api/music-files/:id
echo   GET    http://localhost:3005/api/music-files/code/:code
echo   GET    http://localhost:3005/api/music-files/search?keyword=...
echo   GET    http://localhost:3005/api/music-files/filter/genre/:genreId
echo   POST   http://localhost:3005/api/music-files
echo   PUT    http://localhost:3005/api/music-files/:id
echo   DELETE http://localhost:3005/api/music-files/:id
echo.
echo Press Ctrl+C to stop the server
echo.

json-server --watch music_db.json --port 3005 --routes music_routes.json

pause

