# SensorsHandler

This project consists of three microservices designed to handle sensor data generation, analysis, and summary retrieval efficiently. 
The system solves the problem of managing and querying large volumes of sensor data in real-time by integrating Kafka, PostgreSQL, and Redis, 
while ensuring high performance with minimal latency.

### Microservices:
1. **Data Generator Microservice**: Generates sensor data and sends it to Kafka.
2. **Data Analyser Microservice**: Retrieves sensor data from Kafka and stores it in a PostgreSQL database.
3. **Data Store Microservice**: Provides aggregated summaries of sensor data (e.g., min, max, avg, sum) using a fast Redis cache to ensure quick retrieval even under high load, avoiding slow queries to PostgreSQL.

The system uses Debezium to listen for changes in the PostgreSQL database and updates Kafka, ensuring that the summary microservice always has the most up-to-date data in Redis.

## Technologies Used

- **Backend**: [Spring Boot, Lombok, MapStruct]
- **Database**: [PostgreSQL, Redis]
- **Messaging**: [Kafka, Reactor Kafka, Zookeeper, Debezium]
- **Containerization**: [Docker, Dockerfile, docker-compose.yml]
- **Other Tools**: [Maven, Gson, Liquibase]

## Getting Started

### API Documentation

1. **Data Generator Microservice**:
   - **POST**: ```api/v1/data/send```
   - **EXAMPLE JSON**:
   ```
    {
      "sensorId":10,
      "timestamp":"2025-02-08T16:16:27",
      "measurement":18.5,
      "measurementType": "VOLTAGE"
    }
   ```
   - **POST**: ```api/v1/data/test/send```
   - **EXAMPLE JSON**:
   ```
    {
      "delayInSeconds":5,
      "measurementTypes":[
        "POWER",
        "VOLTAGE",
        "TEMPERATURE"
      ]
   }
   ```

  
3. **Data Store Microservice**:
   - **GET**: ```api/v1/analytics/summary/1?mt=POWER&mt=VOLTAGE&st=AVG```

### Prerequisites

- Java [17]
