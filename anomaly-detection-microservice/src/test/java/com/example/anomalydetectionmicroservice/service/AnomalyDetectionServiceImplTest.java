package com.example.anomalydetectionmicroservice.service;

import com.example.anomalydetectionmicroservice.model.AnomalyEvent;
import com.example.anomalydetectionmicroservice.model.MeasurementType;
import com.example.anomalydetectionmicroservice.model.SensorReading;
import com.example.anomalydetectionmicroservice.repository.RollingStatsRepository;
import com.example.anomalydetectionmicroservice.service.anomaly.AnomalyDetectionServiceImpl;
import com.example.anomalydetectionmicroservice.service.kafka.KafkaProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnomalyDetectionServiceImplTest {

    @Mock
    private RollingStatsRepository rollingStatsRepository;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private AnomalyDetectionServiceImpl anomalyDetectionService;

    private SensorReading reading;

    @BeforeEach
    void setUp() {
        reading = new SensorReading();
        reading.setSensorId(1L);
        reading.setMeasurementType(MeasurementType.VOLTAGE);
        reading.setMeasurement(50.0);
    }

    @Test
    void analyze_shouldAlwaysAddReading(){
        when(rollingStatsRepository.getWindow(anyLong(), any(), anyInt()))
                .thenReturn(Collections.emptyList());

        anomalyDetectionService.analyze(reading);

        verify(rollingStatsRepository).addReading(1L, MeasurementType.VOLTAGE, 50.0);
    }

    @Test
    void analyze_shouldNotSendEvent_whenWindowTooSmall(){
        when(rollingStatsRepository.getWindow(anyLong(), any(), anyInt()))
                .thenReturn(List.of(50.0, 51.0, 49.0, 50.5));

        anomalyDetectionService.analyze(reading);

        verify(kafkaProducerService, never()).send(any());
    }

    @Test
    void analyze_shouldNotSendEvent_whenStdDevIsZero() {
        // all same values → stdDev = 0
        when(rollingStatsRepository.getWindow(anyLong(), any(), anyInt()))
                .thenReturn(List.of(50.0, 50.0, 50.0, 50.0, 50.0));

        anomalyDetectionService.analyze(reading);

        verify(kafkaProducerService, never()).send(any());
    }

    @Test
    void analyze_shouldNotSendEvent_whenZScoreWithinThreshold() {
        // stable window, reading within normal range
        when(rollingStatsRepository.getWindow(anyLong(), any(), anyInt()))
                .thenReturn(List.of(48.0, 50.0, 52.0, 49.0, 51.0,
                        50.0, 48.5, 51.5, 49.5, 50.5));

        reading.setMeasurement(51.0); // not anomalous

        anomalyDetectionService.analyze(reading);

        verify(kafkaProducerService, never()).send(any());
    }

    @Test
    void analyze_shouldSendEvent_whenZScoreExceedsThreshold() {
        // stable window around 50, spike to 999
        when(rollingStatsRepository.getWindow(anyLong(), any(), anyInt()))
                .thenReturn(List.of(48.0, 50.0, 52.0, 49.0, 51.0,
                        50.0, 48.5, 51.5, 49.5, 50.5));

        reading.setMeasurement(999.0); // massive spike

        anomalyDetectionService.analyze(reading);

        verify(kafkaProducerService).send(any(AnomalyEvent.class));
    }

    @Test
    void analyze_shouldPopulateAnomalyEventCorrectly() {
        when(rollingStatsRepository.getWindow(anyLong(), any(), anyInt()))
                .thenReturn(List.of(48.0, 50.0, 52.0, 49.0, 51.0,
                        50.0, 48.5, 51.5, 49.5, 50.5));

        reading.setMeasurement(999.0);

        anomalyDetectionService.analyze(reading);

        ArgumentCaptor<AnomalyEvent> captor = ArgumentCaptor.forClass(AnomalyEvent.class);
        verify(kafkaProducerService).send(captor.capture());

        AnomalyEvent event = captor.getValue();
        assertThat(event.getSensorId()).isEqualTo(1L);
        assertThat(event.getMeasurementType()).isEqualTo(MeasurementType.VOLTAGE);
        assertThat(event.getMeasurement()).isEqualTo(999.0);
        assertThat(event.getZScore()).isGreaterThan(2.5);
        assertThat(event.getMean()).isGreaterThan(0);
        assertThat(event.getStdDev()).isGreaterThan(0);
        assertThat(event.getTimestamp()).isNotNull();
    }

    @Test
    void analyze_shouldSendEvent_whenReadingIsTooLow() {
        // stable window around 50, drop to 1
        when(rollingStatsRepository.getWindow(anyLong(), any(), anyInt()))
                .thenReturn(List.of(48.0, 50.0, 52.0, 49.0, 51.0,
                        50.0, 48.5, 51.5, 49.5, 50.5));

        reading.setMeasurement(1.0); // low spike

        anomalyDetectionService.analyze(reading);

        verify(kafkaProducerService).send(any(AnomalyEvent.class));
    }

}
