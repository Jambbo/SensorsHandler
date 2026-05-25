package com.example.anomalydetectionmicroservice.service.kafka;

import com.example.anomalydetectionmicroservice.model.AnomalyEvent;

public interface KafkaProducerService {
    void send(AnomalyEvent event);
}
