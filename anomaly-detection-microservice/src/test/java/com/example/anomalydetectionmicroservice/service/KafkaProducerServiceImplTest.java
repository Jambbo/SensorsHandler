package com.example.anomalydetectionmicroservice.service;

import com.example.anomalydetectionmicroservice.model.AnomalyEvent;
import com.example.anomalydetectionmicroservice.model.MeasurementType;
import com.example.anomalydetectionmicroservice.service.kafka.KafkaProducerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderResult;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaProducerServiceImplTest {

    @Mock
    private KafkaSender<String, Object> kafkaSender;

    @Mock
    private SenderResult<Object> senderResult;

    @InjectMocks
    private KafkaProducerServiceImpl kafkaProducerService;

    @Test
    void send_shouldCallKafkaSender() {
        AnomalyEvent event = new AnomalyEvent();
        event.setSensorId(1L);
        event.setMeasurementType(MeasurementType.VOLTAGE);
        event.setMeasurement(999.0);
        event.setZScore(3.5);
        event.setMean(50.0);
        event.setStdDev(10.0);
        event.setTimestamp(LocalDateTime.now());

        when(kafkaSender.send(any())).thenReturn(Flux.just(senderResult));

        kafkaProducerService.send(event);

        verify(kafkaSender).send(any());
    }
}