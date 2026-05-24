package com.example.datastoremicroservice;

import com.example.datastoremicroservice.model.Data;
import com.example.datastoremicroservice.model.MeasurementType;
import com.example.datastoremicroservice.model.Notification;
import com.example.datastoremicroservice.model.Threshold;
import com.example.datastoremicroservice.repository.threhsold.ThresholdRepository;
import com.example.datastoremicroservice.service.kafka.KafkaDataService;
import com.example.datastoremicroservice.service.threshold.ThresholdServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThresholdServiceImplTest {

    @Mock
    private ThresholdRepository thresholdRepository;

    @Mock
    private KafkaDataService kafkaDataService;

    @InjectMocks
    private ThresholdServiceImpl thresholdService;

    private Data data;
    private Threshold threshold;

    @BeforeEach
    void setUp() {
        data = new Data();
        data.setSensorId(1L);
        data.setMeasurementType(MeasurementType.VOLTAGE);
        data.setMeasurement(50.0);

        threshold = new Threshold();
        threshold.setSensorId(1L);
        threshold.setMeasurementType(MeasurementType.VOLTAGE);
        threshold.setMinMeasurement(75.0);
        threshold.setMaxMeasurement(100.0);
    }

    @Test
    void setThreshold_shouldSetSensorIdAndDelegate() {
        Threshold input = new Threshold();
        input.setMeasurementType(MeasurementType.VOLTAGE);
        input.setMinMeasurement(10.0);
        input.setMaxMeasurement(90.0);

        thresholdService.setThreshold(1L, input);

        assertThat(input.getSensorId()).isEqualTo(1L);
        verify(thresholdRepository).setThreshold(input);
    }

    @Test
    void getThreshold_shouldReturnThreshold_whenExists() {
        when(thresholdRepository.getThreshold(1L, MeasurementType.VOLTAGE))
                .thenReturn(Optional.of(threshold));

        Threshold result = thresholdService.getThreshold(1L, MeasurementType.VOLTAGE);

        assertThat(result).isEqualTo(threshold);
    }

    @Test
    void getThreshold_shouldThrow_whenNotFound() {
        when(thresholdRepository.getThreshold(1L, MeasurementType.VOLTAGE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> thresholdService.getThreshold(1L, MeasurementType.VOLTAGE))
                .isInstanceOf(java.util.NoSuchElementException.class);
    }

    @Test
    void check_shouldSendNotification_whenBelowMin() {
        data.setMeasurement(50.0); // below min of 75.0
        when(thresholdRepository.getThreshold(1L, MeasurementType.VOLTAGE))
                .thenReturn(Optional.of(threshold));

        thresholdService.check(data);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(kafkaDataService).send(captor.capture());
        assertThat(captor.getValue().getMessage()).contains("below threshold");
    }

    @Test
    void check_shouldSendNotification_whenAboveMax() {
        data.setMeasurement(120.0); // above max of 100.0
        when(thresholdRepository.getThreshold(1L, MeasurementType.VOLTAGE))
                .thenReturn(Optional.of(threshold));

        thresholdService.check(data);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(kafkaDataService).send(captor.capture());
        assertThat(captor.getValue().getMessage()).contains("above threshold");
    }

    @Test
    void check_shouldNotSendNotification_whenWithinRange() {
        data.setMeasurement(80.0); // within 75.0 - 100.0
        when(thresholdRepository.getThreshold(1L, MeasurementType.VOLTAGE))
                .thenReturn(Optional.of(threshold));

        thresholdService.check(data);

        verify(kafkaDataService, never()).send(any());
    }

    @Test
    void check_shouldNotSendNotification_whenNoThresholdSet() {
        when(thresholdRepository.getThreshold(1L, MeasurementType.VOLTAGE))
                .thenReturn(Optional.empty());

        thresholdService.check(data);

        verify(kafkaDataService, never()).send(any());
    }
}