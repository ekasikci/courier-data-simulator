#!/bin/bash

echo "Waiting for Kafka Connect to be ready..."
until curl -f -s http://localhost:8083/connectors > /dev/null 2>&1; do
    echo "Waiting..."
    sleep 5
done

echo "Kafka Connect is ready!"
echo "Registering Debezium MySQL connector..."

curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d @debezium-mysql-connector.json

echo ""
echo "Connector registered! Checking status..."
sleep 2

curl -s http://localhost:8083/connectors/mysql-package-connector/status | jq

echo ""
echo "Setup complete! The connector is now streaming changes from MySQL to Kafka."
echo "Topic created: dbserver.package_db.packages"
