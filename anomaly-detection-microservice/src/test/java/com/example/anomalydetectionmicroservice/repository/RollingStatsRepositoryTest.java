package com.example.anomalydetectionmicroservice.repository;

import com.example.anomalydetectionmicroservice.model.MeasurementType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RollingStatsRepositoryTest {

    @Mock
    private JedisPool jedisPool;

    @Mock
    private Jedis jedis;

    @InjectMocks
    private RollingStatsRepository rollingStatsRepository;

    @BeforeEach
    void setUp() {
        when(jedisPool.getResource()).thenReturn(jedis);
    }

    @Test
    void addReading_shouldPushAndTrim() {
        rollingStatsRepository.addReading(1L, MeasurementType.VOLTAGE, 50.0);

        verify(jedis).lpush("app:anomaly:readings:1:voltage", "50.0");
        verify(jedis).ltrim("app:anomaly:readings:1:voltage", 0, 49);
    }

    @Test
    void addReading_shouldUseCorrectKeyFormat() {
        rollingStatsRepository.addReading(5L, MeasurementType.TEMPERATURE, 22.5);

        verify(jedis).lpush("app:anomaly:readings:5:temperature", "22.5");
    }

    @Test
    void getWindow_shouldReturnParsedDoubles() {
        when(jedis.lrange(anyString(), eq(0L), eq(19L)))
                .thenReturn(List.of("50.0", "51.0", "49.0"));

        List<Double> result = rollingStatsRepository.getWindow(1L, MeasurementType.VOLTAGE, 20);

        assertThat(result).containsExactly(50.0, 51.0, 49.0);
    }

    @Test
    void getWindow_shouldReturnEmptyList_whenNoReadings() {
        when(jedis.lrange(anyString(), eq(0L), eq(19L)))
                .thenReturn(List.of());

        List<Double> result = rollingStatsRepository.getWindow(1L, MeasurementType.VOLTAGE, 20);

        assertThat(result).isEmpty();
    }

    @Test
    void getWindow_shouldUseCorrectWindowSize() {
        when(jedis.lrange(anyString(), eq(0L), eq(9L)))
                .thenReturn(List.of("50.0"));

        rollingStatsRepository.getWindow(1L, MeasurementType.VOLTAGE, 10);

        verify(jedis).lrange("app:anomaly:readings:1:voltage", 0, 9);
    }
}