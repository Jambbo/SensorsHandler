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

## API Documentation

### Service 1: Data Generator Microservice

- **POST** `/send`: Sends sensor data to Kafka.
    - **Request body**: 
    ```json
    {
        "sensorId": 10,
        "timestamp": "2025-02-08T16:16:27",
        "measurement": 18.5,
        "measurementType": "VOLTAGE"
    }
    ```
    - **Response**: HTTP 200 OK if data is sent successfully.

- **POST** `/test/send`: Sends test data for sensor measurements.
    - **Request body**:
    ```json
    {
    "delayInSeconds":5,
    "measurementTypes":[
        "POWER",
        "VOLTAGE",
        "TEMPERATURE"
    ]
    }
    ```
    - **Response**: HTTP 200 OK if test data is sent successfully.

### Service 3: Data Store Microservice

- **GET** `/summary/{sensorId}`: Retrieves aggregated sensor data summary (e.g., `avg`, `max`, `min` values).
    - **Path Variable**: `sensorId` (ID of the sensor)
    - **Request parameters**:
        - `mt` (optional): Set of measurement types (e.g., `VOLTAGE`, `TEMPERATURE`)
        - `st` (optional): Set of summary types (e.g., `sum`, `avg`)
    - **Response**: JSON summary of the requested sensor data.

### Prerequisites

- Java [17]
