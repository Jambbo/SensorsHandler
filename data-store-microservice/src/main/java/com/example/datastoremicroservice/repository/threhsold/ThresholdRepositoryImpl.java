package com.example.datastoremicroservice.repository.threhsold;

import com.example.datastoremicroservice.config.RedisSchema;
import com.example.datastoremicroservice.model.MeasurementType;
import com.example.datastoremicroservice.model.SummaryType;
import com.example.datastoremicroservice.model.Threshold;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class ThresholdRepositoryImpl implements ThresholdRepository {

    private final JedisPool jedisPool;


    @Override
    public void setThreshold(Threshold threshold) {
        try (Jedis jedis = jedisPool.getResource()) {
                String key = RedisSchema.thresholdKey(
                        threshold.getSensorId(),
                        threshold.getMeasurementType()
                );
                jedis.hset(
                        key,
                        SummaryType.MIN.name().toLowerCase(),
                        String.valueOf(threshold.getMinMeasurement())
                );
                jedis.hset(
                        key,
                        SummaryType.MAX.name().toLowerCase(),
                        String.valueOf(threshold.getMaxMeasurement())
                );
        }
    }

    @Override
    public Optional<Threshold> getThreshold(Long sensorId, MeasurementType measurementType) {
        try(Jedis jedis = jedisPool.getResource()){

            String minValue = jedis.hget(
                    RedisSchema.thresholdKey(sensorId,measurementType),
                    SummaryType.MIN.name().toLowerCase()
            );
            String maxValue = jedis.hget(
                    RedisSchema.thresholdKey(sensorId,measurementType),
                    SummaryType.MAX.name().toLowerCase()
            );

            if(minValue==null || maxValue==null)return Optional.empty();

            Threshold threshold = new Threshold();
            threshold.setSensorId(sensorId);
            threshold.setMeasurementType(measurementType);
            threshold.setMinMeasurement(Double.parseDouble(minValue));
            threshold.setMaxMeasurement(Double.parseDouble(maxValue));
            return Optional.of(threshold);
        }
    }
}
