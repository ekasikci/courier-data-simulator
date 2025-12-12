# Courier Data Simulator - Data Engineering Case Study

A scalable, event-driven data pipeline built with Spring Boot, using Debezium CDC for real-time MySQL changes and Kafka Streams for continuous event processing, with optional manual ingestion via REST endpoints and custom strategies.

## 📋 Table of Contents

- [Overview](#overview)
- [Architectures](#architectures)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Project Structure](#project-structure)
- [API Endpoints](#api-endpoints)
- [Configuration Profiles](#configuration-profiles)
- [Testing](#testing)
- [Monitoring](#monitoring)
- [Data Flow](#data-flow)
- [Troubleshooting](#troubleshooting)

## 🎯 Overview

This project implements a complete data pipeline for package delivery tracking:

1. **REST API** - Manual package ingestion endpoints
2. **Data Generation** - Automated package creation with configurable traffic patterns
3. **CDC Pipeline** - Real-time change capture from MySQL using Debezium
4. **Stream Processing** - Transform and filter CDC events using Kafka Streams

## 🏗️ Architectures

This project implements two parallel ingestion paths: the **Automated CDC Stream** and the **Manual Ingestion Path**.

### Automated CDC Stream

```
MySQL Database
    ↓ (CDC - Debezium)
Kafka Topic (Raw CDC): dbserver.package_db.packages
    ↓ (Kafka Streams Processing)
    ↓ (Filter cancelled, Transform to MappedPackage)
Kafka Topic: cdc-mapped-packages
    ↓ (Consumer)
Application Consumer
```

### Manual Ingestion Path

```
MySQL Database
    ↓ (JPA Query with REST Endpoints & Ingestion Strategies)
Package Object
    ↓ (Filter cancelled, Transform to MappedPackage)
Kafka Topic: mapped-package
```

## ✨ Features

### Core Requirements

- REST endpoints for package ingestion (`/kafka/send/{packageId}`, `/kafka/bootstrap`)
- Package to MappedPackage transformation
- Cancelled package filtering
- Kafka integration

### Extended Features

- **Data Generation** - Configurable traffic patterns (constant, mornin rush, spike, realistic, black friday)
- **Multiple Ingestion Strategies**:
  - Batch
  - Micro-batch
  - Streaming
- **Change Data Capture** - Real-time MySQL CDC with Debezium
- **Kafka Streams** - Automated transformation pipeline

## 📦 Prerequisites

- Java 21
- Docker & Docker Compose
- Maven 3.8+

## 🚀 Quick Start

### 1. Clone and Navigate

```bash
cd courier-data-simulator
```

### 2. Start Infrastructure

```bash
chmod +x start.sh
./start.sh
```

This script will:

- Start MySQL, Kafka, Kafka Connect, and Kafka UI
- Wait for all services to be healthy
- Register the Debezium CDC connector

### 3. Build and Run Application

```bash
# Build
mvn clean package -DskipTests

# Run
mvn spring-boot:run
```

### 4. Enable Data Generation (Optional)

Edit `src/main/resources/application.properties`:

```properties
data.generation.enabled=true
```

Then restart the application.

## 📁 Project Structure

```
src/main/java/com/ekasikci/courierdatasimulator/kafka/
├── config/              # Spring and Kafka configuration
├── consumer/            # Kafka message consumers
├── controller/          # REST API endpoints
├── dto/                 # Data transfer objects
├── entity/              # JPA entities
├── repository/          # Data access layer
├── scheduler/           # Data ingestion orchestrator
├── service/             # Business logic
├── simulator/           # Data generation
├── strategy/            # Ingestion strategy implementations
├── streams/             # Kafka Streams processing
└── transformer/         # Package transformation logic
```

## 🔌 API Endpoints

### Send Single Package

```bash
GET /kafka/send/{packageId}
```

**Example:**

```bash
curl http://localhost:8090/kafka/send/1
```

### Bootstrap All Packages

```bash
GET /kafka/bootstrap
```

**Example:**

```bash
curl http://localhost:8090/kafka/bootstrap
```

## ⚙️ Configuration Profiles

The application supports multiple configuration profiles:

### Default Profile

Standard configuration with micro-batch ingestion.

### Streaming Profile

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=streaming
```

### Batch Profile

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=batch
```

### High Load Profile

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=highload
```

## 🧪 Testing

### Run All Tests

```bash
mvn test
```

### Run Specific Test Suite

```bash
# Integration tests
mvn test -Dtest=KafkaIntegrationTest

# Service test
mvn test -Dtest=PackageServiceTest

# Controller tests
mvn test -Dtest=KafkaControllerIntegrationTest
```

## 📊 Monitoring

### Check Kafka Topics

```bash
# List topics
curl http://localhost:8080/api/clusters/local/topics
```

### View messages in Kafka UI, navigate to:

http://localhost:8080

### Check Debezium Connector Status

```bash
curl http://localhost:8083/connectors/mysql-package-connector/status | jq
```

## 🔄 Data Flow

### CDC Stream Path

1. **Package Creation**: Packages are created and inserted in MySQL (via the data generator or manual).
2. **CDC Capture**: Debezium captures changes and publishes to `dbserver.package_db.packages`
3. **Stream Processing**: The Kafka Streams Application consumes the raw CDC events, filters out unwanted records (e.g., deleted/canceled), and performs the required logic to transform the event data into the MappedPackage format.
4. **Output**: Transformed messages published to `cdc-mapped-packages` topic.
5. **Consumption**: Application consumer logs final messages

### Manual Ingestion Path

1. **Package Creation**: Packages are created and inserted in MySQL (via the data generator or manual).
2. **Application/JPA**: Executes SQL query to fetch Package data from MySQL. Manual trigger via `/kafka/send/{packageId}`, `/kafka/bootstrap` or ingestion strategies.
3. **Application Logic**: Performs filtering and transformation into MappedPackage format.
4. **Kafka**: Transformed message published directly to `mapped-package` topic.

## 🛠️ Troubleshooting

### Reset Everything

```bash
chmod +x cleanup.sh
./cleanup.sh
./start.sh
```

### Check Service Health

```bash
# MySQL
docker exec -it mysql-db mysqladmin ping -h localhost -uroot -proot

# Kafka
docker exec -it kafka-broker kafka-topics --bootstrap-server localhost:9092 --list

```
