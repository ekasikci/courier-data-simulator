# Courier Data Simulator - Data Engineering Case Study

A Spring Boot application demonstrating real-time data ingestion patterns with Apache Kafka, featuring Change Data Capture (CDC) with Debezium and stream processing with Kafka Streams.

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Project Structure](#project-structure)
- [API Endpoints](#api-endpoints)
- [Configuration Profiles](#configuration-profiles)
- [Testing](#testing)
- [Monitoring](#monitoring)
- [Troubleshooting](#troubleshooting)

## 🎯 Overview

This project implements a complete data pipeline for package delivery tracking:

1. **REST API** - Manual package ingestion endpoints
2. **Data Generation** - Automated package creation with configurable traffic patterns
3. **CDC Pipeline** - Real-time change capture from MySQL using Debezium
4. **Stream Processing** - Transform and filter CDC events using Kafka Streams
5. **Exactly-Once Semantics** - End-to-end transactional guarantees

## 🏗️ Architecture

```
MySQL Database
    ↓ (CDC - Debezium)
Kafka Topic: dbserver.package_db.packages
    ↓ (Kafka Streams Processing)
    ↓ (Filter cancelled, Transform to MappedPackage)
Kafka Topic: cdc-mapped-packages
    ↓ (Consumer)
Application Consumer
```

### Key Components

- **MySQL** - Source database with package data
- **Debezium** - CDC connector for MySQL
- **Kafka** - Message broker
- **Kafka Streams** - Real-time stream processing
- **Spring Boot** - Application framework

## ✨ Features

### Core Requirements

- REST endpoints for package ingestion (`/kafka/send/{id}`, `/kafka/bootstrap`)
- Package to MappedPackage transformation
- Cancelled package filtering
- Kafka integration

### Extended Features

- **Change Data Capture** - Real-time MySQL CDC with Debezium
- **Kafka Streams** - Automated transformation pipeline
- **Multiple Ingestion Strategies**:
  - Batch
  - Micro-batch
  - Streaming
- **Data Generation** - Configurable traffic patterns (constant, burst, gradual, spike, random)
- **Exactly-Once Processing** - Transaction support across all components

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

# Performance benchmarks
mvn test -Dtest=PackageServiceTest

# Controller tests
mvn test -Dtest=KafkaControllerIntegrationTest
```

## 📊 Monitoring

### Check Kafka Topics

```bash
# List topics
curl http://localhost:8080/api/clusters/local/topics

# View messages in Kafka UI
# Navigate to: http://localhost:8080
```

### Check Debezium Connector Status

```bash
curl http://localhost:8083/connectors/mysql-package-connector/status | jq
```

### View Application Metrics

```bash
curl http://localhost:8090/actuator/metrics
```

## 🔄 Data Flow

1. **Package Creation**: Packages are created in MySQL (via data generator or manual insert)
2. **CDC Capture**: Debezium captures changes and publishes to `dbserver.package_db.packages`
3. **Stream Processing**: Kafka Streams filters and transforms to MappedPackage
4. **Output**: Transformed messages published to `cdc-mapped-packages`
5. **Consumption**: Application consumer processes final messages

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
