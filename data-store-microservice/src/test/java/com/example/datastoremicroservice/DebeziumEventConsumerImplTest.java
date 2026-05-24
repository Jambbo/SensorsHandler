package com.example.datastoremicroservice;

import com.example.datastoremicroservice.model.MeasurementType;
import com.example.datastoremicroservice.service.debezium.DebeziumEventConsumerImpl;
import com.example.datastoremicroservice.service.summary.SummaryService;
import com.example.datastoremicroservice.service.threshold.ThresholdService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DebeziumEventConsumerImplTest {

    @Mock
    private SummaryService summaryService;

    @Mock
    private ThresholdService thresholdService;

    @InjectMocks
    private DebeziumEventConsumerImpl consumer;

    private static final String VALID_MESSAGE = """
            {
              "payload": {
                "id": 1,
                "sensor_id": 42,
                "measurement": 55.5,
                "type": "VOLTAGE",
                "timestamp": 1700000000000000
              }
            }
            """;

    private static final String INVALID_MESSAGE = "not valid json {{{}";

    @Test
    void handle_shouldParseMsgAndCallSummaryAndThreshold() {
        var dataCaptor = ArgumentCaptor.forClass(com.example.datastoremicroservice.model.Data.class);

        consumer.handle(VALID_MESSAGE);

        verify(summaryService).handle(dataCaptor.capture());
        var data = dataCaptor.getValue();
        assertThat(data.getId()).isEqualTo(1L);
        assertThat(data.getSensorId()).isEqualTo(42L);
        assertThat(data.getMeasurement()).isEqualTo(55.5);
        assertThat(data.getMeasurementType()).isEqualTo(MeasurementType.VOLTAGE);
    }

    @Test
    void handle_shouldCallThresholdCheck_afterSummary() {
        var dataCaptor = ArgumentCaptor.forClass(com.example.datastoremicroservice.model.Data.class);

        consumer.handle(VALID_MESSAGE);

        verify(thresholdService).check(dataCaptor.capture());
        assertThat(dataCaptor.getValue().getSensorId()).isEqualTo(42L);
    }

    @Test
    void handle_shouldNotThrow_whenMessageIsInvalid() {
        // should swallow exception gracefully
        consumer.handle(INVALID_MESSAGE);

        verify(summaryService, never()).handle(any());
        verify(thresholdService, never()).check(any());
    }

    @Test
    void handle_shouldNotThrow_whenPayloadFieldsMissing() {
        String missingFields = """
                {
                  "payload": {
                    "id": 1
                  }
                }
                """;

        consumer.handle(missingFields);

        verify(summaryService, never()).handle(any());
        verify(thresholdService, never()).check(any());
    }
}