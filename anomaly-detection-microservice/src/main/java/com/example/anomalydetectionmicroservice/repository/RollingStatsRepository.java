package com.example.anomalydetectionmicroservice.repository;

import com.example.anomalydetectionmicroservice.model.MeasurementType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class RollingStatsRepository {

    private final JedisPool jedisPool;

    // key app:anomaly:readings:1:voltage -> Redis list of last N readings
    public void addReading(Long sensorId, MeasurementType measurementType, double measurement) {
        try (Jedis jedis = jedisPool.getResource()) {
            String key = buildKey(sensorId, measurementType);
            jedis.lpush(key, String.valueOf(measurement));// push to front
            jedis.ltrim(key, 0, 49); // keep last 50 max
        }
    }


    public List<Double> getWindow(Long sensorId, MeasurementType measurementType, int windowSize) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.lrange(buildKey(sensorId, measurementType), 0, windowSize - 1)
                    .stream()
                    .map(Double::parseDouble)
                    .collect(Collectors.toList());
        }
    }

    private String buildKey(Long sensorId, MeasurementType measurementType) {
        return "app:anomaly:readings:" + sensorId + ":" + measurementType.name().toLowerCase();
    }

}
