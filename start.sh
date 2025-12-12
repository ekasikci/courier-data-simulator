#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Courier Data Simulator - Startup${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker is not running. Please start Docker first.${NC}"
    exit 1
fi

# Check for port conflicts and show which containers are using them
echo -e "${YELLOW}🔍 Checking for port conflicts...${NC}"
CONFLICT_FOUND=false

for PORT in 3306 9092 8083 8080; do
    # Check Docker containers using the port
    CONTAINER=$(docker ps --format '{{.Names}}' --filter "publish=$PORT" 2>/dev/null)
    if [ ! -z "$CONTAINER" ]; then
        echo -e "${RED}   ✗ Port $PORT is used by container: $CONTAINER${NC}"
        CONFLICT_FOUND=true
    fi
    
    # Check non-Docker processes
    if command -v lsof &> /dev/null; then
        if lsof -Pi :$PORT -sTCP:LISTEN -t >/dev/null 2>&1; then
            PROCESS=$(lsof -Pi :$PORT -sTCP:LISTEN 2>/dev/null | tail -n 1 | awk '{print $1}')
            if [ "$PROCESS" != "docker-pr" ] && [ ! -z "$PROCESS" ]; then
                echo -e "${RED}   ✗ Port $PORT is used by process: $PROCESS${NC}"
                CONFLICT_FOUND=true
            fi
        fi
    fi
done

if [ "$CONFLICT_FOUND" = true ]; then
    echo ""
    echo -e "${YELLOW}⚠️  Port conflicts detected!${NC}"
    echo -e "${YELLOW}Run this command to clean up:${NC}"
    echo -e "   ${GREEN}./cleanup.sh --force${NC}"
    echo -e "   ${GREEN}sleep 5${NC}"
    echo -e "   ${GREEN}./start.sh${NC}"
    echo ""
    exit 1
fi

echo -e "${GREEN}   ✓ All ports available${NC}"

# Check if docker-compose exists
if ! command -v docker-compose &> /dev/null; then
    echo -e "${YELLOW}⚠️  docker-compose not found, trying docker compose...${NC}"
    DOCKER_COMPOSE="docker compose"
else
    DOCKER_COMPOSE="docker-compose"
fi

echo ""
echo -e "${GREEN}🚀 Starting infrastructure services...${NC}"
$DOCKER_COMPOSE up -d

if [ $? -ne 0 ]; then
    echo ""
    echo -e "${RED}❌ Failed to start services.${NC}"
    echo -e "${YELLOW}Try running:${NC}"
    echo -e "   ${GREEN}./cleanup.sh --force${NC}"
    echo -e "   ${GREEN}sleep 5${NC}"
    echo -e "   ${GREEN}./start.sh${NC}"
    exit 1
fi

echo ""
echo -e "${YELLOW}⏳ Waiting for services to be healthy...${NC}"
sleep 5

# Wait for MySQL
echo -e "${BLUE}   Checking MySQL...${NC}"
for i in {1..15}; do
    if docker exec mysql-db mysqladmin ping -h localhost -uroot -proot --silent > /dev/null 2>&1; then
        echo -e "${GREEN}   ✓ MySQL is ready${NC}"
        break
    fi
    if [ $i -eq 15 ]; then
        echo -e "${RED}   ✗ MySQL failed to start${NC}"
        echo -e "${YELLOW}   Check logs: docker-compose logs mysql-db${NC}"
        exit 1
    fi
    sleep 1
done

# Wait for Kafka
echo -e "${BLUE}   Checking Kafka...${NC}"
for i in {1..15}; do
    if docker exec kafka-broker kafka-topics --bootstrap-server localhost:9092 --list > /dev/null 2>&1; then
        echo -e "${GREEN}   ✓ Kafka is ready${NC}"
        break
    fi
    if [ $i -eq 15 ]; then
        echo -e "${RED}   ✗ Kafka failed to start${NC}"
        echo -e "${YELLOW}   Check logs: docker-compose logs kafka-broker${NC}"
        exit 1
    fi
    sleep 1
done

# Wait for Kafka Connect
echo -e "${BLUE}   Checking Kafka Connect...${NC}"
for i in {1..20}; do
    if curl -sf http://localhost:8083/ > /dev/null 2>&1; then
        echo -e "${GREEN}   ✓ Kafka Connect is ready${NC}"
        break
    fi
    if [ $i -eq 20 ]; then
        echo -e "${RED}   ✗ Kafka Connect failed to start${NC}"
        echo -e "${YELLOW}   Check logs: docker-compose logs kafka-connect${NC}"
        exit 1
    fi
    sleep 2
done

sleep 3

# Register Debezium connector
echo ""
echo -e "${GREEN}📝 Registering Debezium CDC connector...${NC}"

# Delete existing connector if present
curl -X DELETE http://localhost:8083/connectors/mysql-package-connector 2>/dev/null

sleep 2

# Register new connector
RESPONSE=$(curl -s -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d @debezium-mysql-connector.json)

if echo "$RESPONSE" | grep -q "error"; then
    echo -e "${RED}❌ Failed to register connector:${NC}"
    echo "$RESPONSE"
    exit 1
fi

sleep 3

# Check connector status
echo -e "${BLUE}   Checking connector status...${NC}"
STATUS=$(curl -s http://localhost:8083/connectors/mysql-package-connector/status)

if echo "$STATUS" | grep -q '"state":"RUNNING"'; then
    echo -e "${GREEN}   ✓ Connector is running${NC}"
else
    echo -e "${YELLOW}   ⚠️  Connector status:${NC}"
    echo "$STATUS" | grep -o '"state":"[^"]*"' || echo "Unknown"
fi

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}✅ Infrastructure is ready!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "${BLUE}📋 Available Services:${NC}"
echo -e "   ${YELLOW}→${NC} MySQL:         localhost:3306"
echo -e "   ${YELLOW}→${NC} Kafka:         localhost:9092"
echo -e "   ${YELLOW}→${NC} Kafka Connect: http://localhost:8083"
echo -e "   ${YELLOW}→${NC} Kafka UI:      http://localhost:8080"
echo ""
echo -e "${BLUE}📝 Next Steps:${NC}"
echo -e "   1. Build:  ${GREEN}./mvnw clean package -DskipTests${NC}"
echo -e "   2. Run:    ${GREEN}./mvnw spring-boot:run${NC}"
echo ""