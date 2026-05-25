package com.example.anomalydetectionmicroservice.service.kafka;

import com.example.anomalydetectionmicroservice.service.anomaly.AnomalyDetectionService;
import com.example.anomalydetectionmicroservice.web.mapper.SensorReadingMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class KafkaConsumerServiceImpl implements KafkaConsumerService {

    private final AnomalyDetectionService anomalyDetectionService;
    private final SensorReadingMapper sensorReadingMapper;

    @KafkaListener(topics = "data", groupId = "anomaly-detection-group")
    @Override
    public void consume(String message) {
        try {

            anomalyDetectionService.analyze(
                    sensorReadingMapper.fromDebeziumPayload(message)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
