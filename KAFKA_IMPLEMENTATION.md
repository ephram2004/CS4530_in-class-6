# Kafka Metrics Implementation

## Overview

This implementation replaces HTTP-based metrics collection with Kafka events for better scalability and decoupling.

## Architecture

```
Client → REServer (Producer) → Kafka Topic → REMetrics (Consumer) → Database
```

### Flow:
1. **Client** hits any endpoint in REServer
2. **REServer** emits Kafka event immediately
3. **REMetrics** (Kafka consumer) receives event and updates database
4. **RESales** processes the actual request
5. **REServer** returns response to client

## Files Modified/Created

### REServer (Producer)
- **Modified**: `REServer/src/main/java/app/REServer.java`
- **Created**: `REServer/src/main/java/kafka/MetricsKafkaProducer.java`
- **Added**: Kafka dependencies to `REServer/pom.xml`

### REMetrics (Consumer)
- **Modified**: `remetrics/src/main/java/app/REMetrics.java`
- **Created**: 
  - `remetrics/src/main/java/kafka/MetricsKafkaListener.java`
  - `remetrics/src/main/java/kafka/MetricsProcessor.java`
  - `remetrics/src/main/java/kafka/MetricsProcessorImpl.java`
- **Added**: Kafka dependencies to `remetrics/pom.xml`

### Infrastructure
- **Created**: `kafka-docker-compose.yml` for Kafka setup

## When Events Are Emitted

**Strategy**: Emit on every API hit (not just database operations)

**Events emitted for:**
- `/sales/postcode/{postcode}` → `{"metric_name": "postcode", "metric_id": "2000", "action": "access"}`
- `/sales/average/{postcode}` → `{"metric_name": "postcode", "metric_id": "2000", "action": "access"}`
- `/sales/price-history/propertyId/{propertyID}` → `{"metric_name": "propertyid", "metric_id": "123", "action": "access"}`
- `/sales/{saleID}` → `{"metric_name": "saleid", "metric_id": "456", "action": "access"}`

## Setup Instructions

### 1. Start Kafka Infrastructure
```bash
docker-compose -f kafka-docker-compose.yml up -d
```

### 2. Compile the Project
```bash
mvn clean compile
```

### 3. Start Services
```bash
# Terminal 1: Start REMetrics (Kafka Consumer)
cd remetrics
mvn exec:java -Dexec.mainClass="app.REMetrics"

# Terminal 2: Start RESales
cd resales  
mvn exec:java -Dexec.mainClass="app.RESales"

# Terminal 3: Start REServer (Kafka Producer)
cd REServer
mvn exec:java -Dexec.mainClass="app.REServer"
```

### 4. Test the Implementation
```bash
# Test a request that will emit Kafka events
curl "http://localhost:7070/sales/postcode/2000"
```

## Benefits of Kafka Approach

1. **Decoupling**: REServer doesn't need to wait for metrics processing
2. **Scalability**: Kafka can handle high throughput
3. **Reliability**: Events are persisted and can be replayed
4. **Real-time**: Immediate event emission
5. **Fault Tolerance**: If metrics service is down, events are queued

## Monitoring

- **Kafka Topic**: `real-estate-metrics`
- **Consumer Group**: `metrics-consumer-group`
- **Bootstrap Servers**: `localhost:9092`

## Event Format
```json
{
  "metric_name": "postcode",
  "metric_id": "2000", 
  "action": "access",
  "timestamp": 1703123456789
}
```