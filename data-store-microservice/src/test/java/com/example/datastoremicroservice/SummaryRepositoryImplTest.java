package com.example.datastoremicroservice.repository.summary;

import com.example.datastoremicroservice.model.Data;
import com.example.datastoremicroservice.model.MeasurementType;
import com.example.datastoremicroservice.model.SummaryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // fixes PotentialStubbingProblem
class SummaryRepositoryImplTest {

    @Mock
    private JedisPool jedisPool;

    @Mock
    private Jedis jedis;

    @InjectMocks
    private SummaryRepositoryImpl summaryRepository;

    private Data data;

    @BeforeEach
    void setUp() {
        when(jedisPool.getResource()).thenReturn(jedis);

        data = new Data();
        data.setSensorId(1L);
        data.setMeasurementType(MeasurementType.VOLTAGE);
        data.setMeasurement(50.0);
    }

    // ── handle() ──────────────────────────────────────────────

    @Test
    void handle_shouldRegisterSensor_whenNotExists() {
        when(jedis.sismember(anyString(), anyString())).thenReturn(false);
        when(jedis.hget(anyString(), eq("min"))).thenReturn(null);
        when(jedis.hget(anyString(), eq("max"))).thenReturn(null);
        when(jedis.hget(anyString(), eq("sum"))).thenReturn("50.0");
        when(jedis.hget(anyString(), eq("counter"))).thenReturn(null);
        when(jedis.hset(anyString(), eq("counter"), anyString())).thenReturn(1L);

        summaryRepository.handle(data);

        verify(jedis).sadd(anyString(), eq("1"));
    }

    @Test
    void handle_shouldNotRegisterSensor_whenAlreadyExists() {
        when(jedis.sismember(anyString(), anyString())).thenReturn(true);
        when(jedis.hget(anyString(), eq("min"))).thenReturn(null);
        when(jedis.hget(anyString(), eq("max"))).thenReturn(null);
        when(jedis.hget(anyString(), eq("sum"))).thenReturn("50.0");
        when(jedis.hget(anyString(), eq("counter"))).thenReturn(null);
        when(jedis.hset(anyString(), eq("counter"), anyString())).thenReturn(1L);

        summaryRepository.handle(data);

        verify(jedis, never()).sadd(anyString(), anyString());
    }

    @Test
    void handle_shouldSetMin_whenNoExistingValue() {
        when(jedis.sismember(anyString(), anyString())).thenReturn(true);
        when(jedis.hget(anyString(), eq("min"))).thenReturn(null);
        when(jedis.hget(anyString(), eq("max"))).thenReturn(null);
        // fix: same here
        when(jedis.hget(anyString(), eq("sum"))).thenReturn("50.0");
        when(jedis.hget(anyString(), eq("counter"))).thenReturn(null);
        when(jedis.hset(anyString(), eq("counter"), anyString())).thenReturn(1L);

        summaryRepository.handle(data);

        verify(jedis).hset(anyString(), eq("min"), eq("50.0"));
    }

    @Test
    void handle_shouldUpdateMin_whenNewValueIsLower() {
        when(jedis.sismember(anyString(), anyString())).thenReturn(true);
        when(jedis.hget(anyString(), eq("min"))).thenReturn("80.0");
        when(jedis.hget(anyString(), eq("max"))).thenReturn("100.0");
        when(jedis.hget(anyString(), eq("sum"))).thenReturn("80.0");
        when(jedis.hget(anyString(), eq("counter"))).thenReturn("1");
        when(jedis.hincrBy(anyString(), eq("counter"), eq(1L))).thenReturn(2L);

        summaryRepository.handle(data); // 50.0 < 80.0

        verify(jedis).hset(anyString(), eq("min"), eq("50.0"));
    }

    @Test
    void handle_shouldNotUpdateMin_whenNewValueIsHigher() {
        when(jedis.sismember(anyString(), anyString())).thenReturn(true);
        when(jedis.hget(anyString(), eq("min"))).thenReturn("20.0");
        when(jedis.hget(anyString(), eq("max"))).thenReturn("100.0");
        when(jedis.hget(anyString(), eq("sum"))).thenReturn("20.0");
        when(jedis.hget(anyString(), eq("counter"))).thenReturn("1");
        when(jedis.hincrBy(anyString(), eq("counter"), eq(1L))).thenReturn(2L);

        summaryRepository.handle(data); // 50.0 > 20.0

        verify(jedis, never()).hset(anyString(), eq("min"), anyString());
    }

    @Test
    void handle_shouldUpdateMax_whenNewValueIsHigher() {
        data.setMeasurement(120.0);
        when(jedis.sismember(anyString(), anyString())).thenReturn(true);
        when(jedis.hget(anyString(), eq("min"))).thenReturn("20.0");
        when(jedis.hget(anyString(), eq("max"))).thenReturn("100.0");
        when(jedis.hget(anyString(), eq("sum"))).thenReturn("100.0");
        when(jedis.hget(anyString(), eq("counter"))).thenReturn("1");
        when(jedis.hincrBy(anyString(), eq("counter"), eq(1L))).thenReturn(2L);

        summaryRepository.handle(data); // 120.0 > 100.0

        verify(jedis).hset(anyString(), eq("max"), eq("120.0"));
    }

    // ── findBySensorId() ──────────────────────────────────────

    @Test
    void findBySensorId_shouldReturnEmpty_whenSensorNotRegistered() {
        when(jedis.sismember(anyString(), eq("1"))).thenReturn(false);

        var result = summaryRepository.findBySensorId(
                1L, Set.of(MeasurementType.VOLTAGE), Set.of(SummaryType.MIN)
        );

        assertThat(result).isEmpty();
    }

    @Test
    void findBySensorId_shouldReturnSummary_whenSensorExists() {
        when(jedis.sismember(anyString(), eq("1"))).thenReturn(true);
        when(jedis.hget(anyString(), eq("min"))).thenReturn("10.0");
        // fix 2: counter must be a valid Long string, not a double
        when(jedis.hget(anyString(), eq("counter"))).thenReturn("5");

        var result = summaryRepository.findBySensorId(
                1L, Set.of(MeasurementType.VOLTAGE), Set.of(SummaryType.MIN)
        );

        assertThat(result).isPresent();
        assertThat(result.get().getSensorId()).isEqualTo(1L);
    }

    @Test
    void findBySensorId_shouldUseAllMeasurementTypes_whenMeasurementTypesEmpty() {
        when(jedis.sismember(anyString(), anyString())).thenReturn(true);
        // fix 2: return proper types per field — double for values, Long for counter
        when(jedis.hget(anyString(), eq("min"))).thenReturn("10.0");
        when(jedis.hget(anyString(), eq("max"))).thenReturn("90.0");
        when(jedis.hget(anyString(), eq("sum"))).thenReturn("100.0");
        when(jedis.hget(anyString(), eq("avg"))).thenReturn("50.0");
        when(jedis.hget(anyString(), eq("counter"))).thenReturn("2");

        var result = summaryRepository.findBySensorId(
                1L, Set.of(), Set.of(SummaryType.MIN)
        );

        assertThat(result).isPresent();
    }

    @Test
    void findBySensorId_shouldUseAllSummaryTypes_whenSummaryTypesEmpty() {
        when(jedis.sismember(anyString(), anyString())).thenReturn(true);
        // fix 2: same here
        when(jedis.hget(anyString(), eq("min"))).thenReturn("10.0");
        when(jedis.hget(anyString(), eq("max"))).thenReturn("90.0");
        when(jedis.hget(anyString(), eq("sum"))).thenReturn("100.0");
        when(jedis.hget(anyString(), eq("avg"))).thenReturn("50.0");
        when(jedis.hget(anyString(), eq("counter"))).thenReturn("2");

        var result = summaryRepository.findBySensorId(
                1L, Set.of(MeasurementType.VOLTAGE), Set.of()
        );

        assertThat(result).isPresent();
    }
}