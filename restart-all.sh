#!/bin/bash

echo "🧹 Cleaning up everything..."

# Stop Spring Boot if running
pkill -f "CourierDataSimulator" || true

# Stop and remove all containers
docker-compose down -v

# Clean Kafka Streams state
rm -rf /tmp/kafka-streams/*

# Clean target directory
rm -rf target/

echo "✅ Cleanup complete!"
echo ""
echo "🚀 Starting services..."

# Start Docker services
docker-compose up -d

echo "⏳ Waiting for services to be healthy..."
sleep 5

# Wait for Kafka Connect
echo "⏳ Waiting for Kafka Connect..."
until curl -f -s http://localhost:8083/ > /dev/null 2>&1; do
    echo "   Still waiting for Kafka Connect..."
    sleep 5
done

echo "✅ Kafka Connect is ready!"
sleep 5

# Register Debezium connector
echo "📝 Registering Debezium connector..."
curl -X DELETE http://localhost:8083/connectors/mysql-package-connector 2>/dev/null || true
sleep 2

curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d @debezium-mysql-connector.json

echo ""
echo "⏳ Checking connector status..."
sleep 3

curl -s http://localhost:8083/connectors/mysql-package-connector/status | jq

echo ""
echo "✅ All services ready!"
echo ""
echo "📋 Next steps:"
echo "1. Build: ./mvnw clean package -DskipTests"
echo "2. Run: ./mvnw spring-boot:run"
echo "3. Enable data generation in application.properties: data.generation.enabled=true"
echo ""
echo "🔍 Monitor with:"
echo "   - Kafka UI: http://localhost:8080"
echo "   - Connector status: curl http://localhost:8083/connectors/mysql-package-connector/status | jq"