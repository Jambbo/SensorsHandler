package com.example.datastoremicroservice;

import com.example.datastoremicroservice.model.Data;
import com.example.datastoremicroservice.model.MeasurementType;
import com.example.datastoremicroservice.model.Summary;
import com.example.datastoremicroservice.model.SummaryType;
import com.example.datastoremicroservice.model.exception.SensorNotFoundException;
import com.example.datastoremicroservice.repository.summary.SummaryRepository;
import com.example.datastoremicroservice.service.summary.SummaryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SummaryServiceImplTest {

    @Mock
    private SummaryRepository summaryRepository;

    @InjectMocks
    private SummaryServiceImpl summaryService;

    @Test
    void get_shouldReturnSummary_whenSensorExists() {
        Summary summary = new Summary();
        when(summaryRepository.findBySensorId(eq(1L), any(), any()))
                .thenReturn(Optional.of(summary));

        Summary result = summaryService.get(1L, Set.of(MeasurementType.VOLTAGE), Set.of(SummaryType.MIN));

        assertThat(result).isEqualTo(summary);
    }

    @Test
    void get_shouldThrowSensorNotFoundException_whenNotFound() {
        when(summaryRepository.findBySensorId(eq(1L), any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> summaryService.get(1L, Set.of(MeasurementType.VOLTAGE), Set.of(SummaryType.MIN)))
                .isInstanceOf(SensorNotFoundException.class);
    }

    @Test
    void get_shouldUseAllMeasurementTypes_whenNull() {
        Summary summary = new Summary();
        when(summaryRepository.findBySensorId(eq(1L), eq(Set.of(MeasurementType.values())), any()))
                .thenReturn(Optional.of(summary));

        Summary result = summaryService.get(1L, null, Set.of(SummaryType.MIN));

        assertThat(result).isEqualTo(summary);
    }

    @Test
    void get_shouldUseAllSummaryTypes_whenNull() {
        Summary summary = new Summary();
        when(summaryRepository.findBySensorId(eq(1L), any(), eq(Set.of(SummaryType.values()))))
                .thenReturn(Optional.of(summary));

        Summary result = summaryService.get(1L, Set.of(MeasurementType.VOLTAGE), null);

        assertThat(result).isEqualTo(summary);
    }

    @Test
    void handle_shouldDelegateToRepository() {
        Data data = new Data();
        data.setSensorId(1L);

        summaryService.handle(data);

        verify(summaryRepository).handle(data);
    }
}