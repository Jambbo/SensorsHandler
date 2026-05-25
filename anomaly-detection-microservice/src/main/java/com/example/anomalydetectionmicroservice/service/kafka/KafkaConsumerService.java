package com.example.anomalydetectionmicroservice.service.kafka;

public interface KafkaConsumerService {
    void consume(String message);
}
