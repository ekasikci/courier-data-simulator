@echo off
setlocal enabledelayedexpansion

echo ========================================
echo   Courier Data Simulator - Startup
echo ========================================
echo.

REM Check if Docker is running
docker info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker is not running. Please start Docker first.
    pause
    exit /b 1
)

echo [INFO] Starting infrastructure services...
docker-compose up -d

echo.
echo [INFO] Waiting for services to be healthy...
timeout /t 10 /nobreak >nul

REM Wait for MySQL
echo [INFO] Checking MySQL...
set MAX_ATTEMPTS=30
set ATTEMPT=0
:MYSQL_LOOP
docker exec mysql-db mysqladmin ping -h localhost -uroot -proot --silent >nul 2>&1
if not errorlevel 1 (
    echo [SUCCESS] MySQL is ready
    goto KAFKA_CHECK
)
set /a ATTEMPT+=1
if !ATTEMPT! geq %MAX_ATTEMPTS% (
    echo [ERROR] MySQL failed to start
    pause
    exit /b 1
)
timeout /t 2 /nobreak >nul
goto MYSQL_LOOP

:KAFKA_CHECK
echo [INFO] Checking Kafka...
set ATTEMPT=0
:KAFKA_LOOP
docker exec kafka-broker kafka-topics --bootstrap-server localhost:9092 --list >nul 2>&1
if not errorlevel 1 (
    echo [SUCCESS] Kafka is ready
    goto CONNECT_CHECK
)
set /a ATTEMPT+=1
if !ATTEMPT! geq %MAX_ATTEMPTS% (
    echo [ERROR] Kafka failed to start
    pause
    exit /b 1
)
timeout /t 2 /nobreak >nul
goto KAFKA_LOOP

:CONNECT_CHECK
echo [INFO] Checking Kafka Connect...
set MAX_ATTEMPTS=40
set ATTEMPT=0
:CONNECT_LOOP
curl -sf http://localhost:8083/ >nul 2>&1
if not errorlevel 1 (
    echo [SUCCESS] Kafka Connect is ready
    goto REGISTER_CONNECTOR
)
set /a ATTEMPT+=1
if !ATTEMPT! geq %MAX_ATTEMPTS% (
    echo [ERROR] Kafka Connect failed to start
    pause
    exit /b 1
)
timeout /t 3 /nobreak >nul
goto CONNECT_LOOP

:REGISTER_CONNECTOR
timeout /t 5 /nobreak >nul

echo.
echo [INFO] Registering Debezium CDC connector...

REM Delete existing connector
curl -X DELETE http://localhost:8083/connectors/mysql-package-connector >nul 2>&1
timeout /t 2 /nobreak >nul

REM Register new connector
curl -s -X POST http://localhost:8083/connectors -H "Content-Type: application/json" -d @debezium-mysql-connector.json >nul

timeout /t 3 /nobreak >nul

REM Check connector status
echo [INFO] Checking connector status...
curl -s http://localhost:8083/connectors/mysql-package-connector/status | findstr "RUNNING" >nul
if not errorlevel 1 (
    echo [SUCCESS] Connector is running
) else (
    echo [WARNING] Connector may not be running correctly
)

echo.
echo ========================================
echo [SUCCESS] Infrastructure is ready!
echo ========================================
echo.
echo Available Services:
echo    MySQL:         localhost:3306
echo    Kafka:         localhost:9092
echo    Kafka Connect: http://localhost:8083
echo    Kafka UI:      http://localhost:8080
echo.
echo Next Steps:
echo    1. Build:  mvnw.cmd clean package -DskipTests
echo    2. Run:    mvnw.cmd spring-boot:run
echo.
pause