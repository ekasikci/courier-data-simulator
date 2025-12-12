#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Cleanup - Courier Data Simulator${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Check for --force flag
FORCE_KILL=false
if [ "$1" == "--force" ]; then
    FORCE_KILL=true
    echo -e "${YELLOW}⚠️  Force mode enabled - will aggressively clean all resources${NC}"
    echo ""
fi

# Check if docker-compose exists
if ! command -v docker-compose &> /dev/null; then
    DOCKER_COMPOSE="docker compose"
else
    DOCKER_COMPOSE="docker-compose"
fi

# Stop Spring Boot application if running
echo -e "${YELLOW}🛑 Stopping Spring Boot application...${NC}"
pkill -f "CourierDataSimulator" 2>/dev/null && echo -e "${GREEN}   ✓ Application stopped${NC}" || echo -e "${BLUE}   ℹ  No running application found${NC}"

# Stop and remove Docker containers
echo -e "${YELLOW}🐳 Stopping Docker containers...${NC}"
$DOCKER_COMPOSE down -v 2>/dev/null

# Additional aggressive cleanup if needed
if [ "$FORCE_KILL" = true ]; then
    echo ""
    echo -e "${YELLOW}🔥 Performing aggressive cleanup...${NC}"
    
    # Stop all containers with our project name
    echo -e "${BLUE}   Stopping all related containers...${NC}"
    docker ps -a | grep -E "mysql-db|kafka-broker|kafka-connect|kafka-ui" | awk '{print $1}' | xargs -r docker stop 2>/dev/null
    docker ps -a | grep -E "mysql-db|kafka-broker|kafka-connect|kafka-ui" | awk '{print $1}' | xargs -r docker rm -f 2>/dev/null
    
    # Remove any containers using port 3306
    echo -e "${BLUE}   Removing containers on port 3306...${NC}"
    CONTAINER_3306=$(docker ps -a | grep "0.0.0.0:3306" | awk '{print $1}')
    if [ ! -z "$CONTAINER_3306" ]; then
        docker rm -f $CONTAINER_3306 2>/dev/null && echo -e "${GREEN}   ✓ Removed container on port 3306${NC}"
    fi
    
    # Remove any containers using port 9092
    echo -e "${BLUE}   Removing containers on port 9092...${NC}"
    CONTAINER_9092=$(docker ps -a | grep "0.0.0.0:9092" | awk '{print $1}')
    if [ ! -z "$CONTAINER_9092" ]; then
        docker rm -f $CONTAINER_9092 2>/dev/null && echo -e "${GREEN}   ✓ Removed container on port 9092${NC}"
    fi
    
    # Remove volumes
    echo -e "${BLUE}   Removing volumes...${NC}"
    docker volume rm courier-data-simulator_mysql-data 2>/dev/null
    docker volume rm courier-data-simulator_kafka-data 2>/dev/null
    
    # Remove network
    echo -e "${BLUE}   Removing network...${NC}"
    docker network rm courier-data-simulator_default 2>/dev/null
    
    # Kill any processes on required ports (last resort)
    echo -e "${BLUE}   Checking for processes on ports...${NC}"
    
    for PORT in 3306 9092 8083 8080; do
        if command -v lsof &> /dev/null; then
            PID=$(lsof -ti:$PORT 2>/dev/null)
            if [ ! -z "$PID" ]; then
                kill -9 $PID 2>/dev/null && echo -e "${GREEN}   ✓ Killed process on port $PORT${NC}"
            fi
        elif command -v fuser &> /dev/null; then
            fuser -k $PORT/tcp 2>/dev/null && echo -e "${GREEN}   ✓ Killed process on port $PORT${NC}"
        fi
    done
    
    sleep 2
fi

# Clean Kafka Streams state
echo -e "${YELLOW}🧹 Cleaning Kafka Streams state...${NC}"
rm -rf /tmp/kafka-streams/* 2>/dev/null && echo -e "${GREEN}   ✓ Kafka Streams state cleaned${NC}" || echo -e "${BLUE}   ℹ  No Kafka Streams state found${NC}"

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}✅ Cleanup complete!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

if [ "$FORCE_KILL" = true ]; then
    echo -e "${YELLOW}💡 Tip: Wait 5 seconds before running ./start.sh${NC}"
    echo ""
fi

echo -e "${BLUE}📝 To restart everything:${NC}"
echo -e "   ${GREEN}./start.sh${NC}"
echo ""