package com.example.anomalydetectionmicroservice.service;

import com.example.anomalydetectionmicroservice.model.MeasurementType;
import com.example.anomalydetectionmicroservice.model.SensorReading;
import com.example.anomalydetectionmicroservice.service.anomaly.AnomalyDetectionService;
import com.example.anomalydetectionmicroservice.service.kafka.KafkaConsumerServiceImpl;
import com.example.anomalydetectionmicroservice.web.mapper.SensorReadingMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KafkaConsumerServiceImplTest {

    @Mock
    private AnomalyDetectionService anomalyDetectionService;

    @Mock
    private SensorReadingMapper sensorReadingMapper;

    @InjectMocks
    private KafkaConsumerServiceImpl kafkaConsumerService;


    @Test
    void consume_shouldMapAndDelegate(){
        SensorReading reading = new SensorReading();
        reading.setSensorId(1L);
        reading.setMeasurementType(MeasurementType.VOLTAGE);

        when(sensorReadingMapper.fromDebeziumPayload(anyString())).thenReturn(reading);

        kafkaConsumerService.consume("any message");

        verify(sensorReadingMapper).fromDebeziumPayload("any message");
        verify(anomalyDetectionService).analyze(reading);
    }

    @Test
    void consume_shouldNotThrow_whenMapperThrows(){
        when(sensorReadingMapper.fromDebeziumPayload(anyString()))
                .thenThrow(new RuntimeException("parse error"));

        kafkaConsumerService.consume("bad message");

        verify(anomalyDetectionService, never()).analyze(any());
    }

    @Test
    void consume_shoutNotThrow_whenServiceThrows(){
        SensorReading reading = new SensorReading();
        when(sensorReadingMapper.fromDebeziumPayload(anyString())).thenReturn(reading);
        doThrow(new RuntimeException("service error"))
                .when(anomalyDetectionService).analyze(any());
        kafkaConsumerService.consume("any message");
        // should swallow exception gracefully


    }

}
