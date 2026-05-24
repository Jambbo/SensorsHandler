package com.example.datastoremicroservice;

import com.example.datastoremicroservice.model.MeasurementType;
import com.example.datastoremicroservice.model.Threshold;
import com.example.datastoremicroservice.repository.threhsold.ThresholdRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThresholdRepositoryImplTest {

    @Mock
    private JedisPool jedisPool;

    @Mock
    private Jedis jedis;

    @InjectMocks
    private ThresholdRepositoryImpl thresholdRepository;

    @BeforeEach
    void setUp() {
        when(jedisPool.getResource()).thenReturn(jedis);
    }

    @Test
    void setThreshold_shouldStoreMinAndMaxInRedis() {
        Threshold threshold = new Threshold();
        threshold.setSensorId(1L);
        threshold.setMeasurementType(MeasurementType.VOLTAGE);
        threshold.setMinMeasurement(10.0);
        threshold.setMaxMeasurement(90.0);

        thresholdRepository.setThreshold(threshold);

        verify(jedis).hset(anyString(), eq("min"), eq("10.0"));
        verify(jedis).hset(anyString(), eq("max"), eq("90.0"));
    }

    @Test
    void getThreshold_shouldReturnThreshold_whenValuesExist() {
        when(jedis.hget(anyString(), eq("min"))).thenReturn("10.0");
        when(jedis.hget(anyString(), eq("max"))).thenReturn("90.0");

        Optional<Threshold> result = thresholdRepository.getThreshold(1L, MeasurementType.VOLTAGE);

        assertThat(result).isPresent();
        assertThat(result.get().getMinMeasurement()).isEqualTo(10.0);
        assertThat(result.get().getMaxMeasurement()).isEqualTo(90.0);
        assertThat(result.get().getSensorId()).isEqualTo(1L);
        assertThat(result.get().getMeasurementType()).isEqualTo(MeasurementType.VOLTAGE);
    }

    @Test
    void getThreshold_shouldReturnEmpty_whenMinIsNull() {
        when(jedis.hget(anyString(), eq("min"))).thenReturn(null);
        when(jedis.hget(anyString(), eq("max"))).thenReturn("90.0");

        Optional<Threshold> result = thresholdRepository.getThreshold(1L, MeasurementType.VOLTAGE);

        assertThat(result).isEmpty();
    }

    @Test
    void getThreshold_shouldReturnEmpty_whenMaxIsNull() {
        when(jedis.hget(anyString(), eq("min"))).thenReturn("10.0");
        when(jedis.hget(anyString(), eq("max"))).thenReturn(null);

        Optional<Threshold> result = thresholdRepository.getThreshold(1L, MeasurementType.VOLTAGE);

        assertThat(result).isEmpty();
    }
}