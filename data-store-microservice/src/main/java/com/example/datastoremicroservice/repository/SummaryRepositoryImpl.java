package com.example.datastoremicroservice.repository;

import com.example.datastoremicroservice.config.RedisSchema;
import com.example.datastoremicroservice.model.Data;
import com.example.datastoremicroservice.model.MeasurementType;
import com.example.datastoremicroservice.model.Summary;
import com.example.datastoremicroservice.model.SummaryType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Optional;
import java.util.Set;


@Repository
@RequiredArgsConstructor
public class SummaryRepositoryImpl implements SummaryRepository {

    private final JedisPool jedisPool;


    @Override
    public Optional<Summary> findBySensorId(
            long sensorId,
            Set<MeasurementType> measurementTypes,
            Set<SummaryType> summaryTypes
    ) {
        try (Jedis jedis = jedisPool.getResource()) {
            if (!jedis.sismember(
                    RedisSchema.sensorKeys(),
                    String.valueOf(sensorId)
            )) {
                return Optional.empty();
            }
            if (measurementTypes.isEmpty() && !summaryTypes.isEmpty()) {
                return getSummary(
                        sensorId,
                        Set.of(MeasurementType.values()),
                        summaryTypes,
                        jedis
                );
            } else if (!measurementTypes.isEmpty() && summaryTypes.isEmpty()) {
                return getSummary(
                        sensorId,
                        measurementTypes,
                        Set.of(SummaryType.values()),
                        jedis
                );
            } else {
                return getSummary(
                        sensorId,
                        measurementTypes,
                        summaryTypes,
                        jedis
                );
            }
        }
    }

    private Optional<Summary> getSummary(
            long sensorId,
            Set<MeasurementType> measurementTypes,
            Set<SummaryType> summaryTypes,
            Jedis jedis
    ) {
        Summary summary = new Summary();
        summary.setSensorId(sensorId);

        measurementTypes.forEach(mt -> {
            summaryTypes.forEach(st -> {

                Summary.SummaryEntry entry = new Summary.SummaryEntry();
                entry.setType(st);
                String value = jedis.hget(
                        RedisSchema.summaryKey(sensorId, mt),
                        st.name().toLowerCase()
                );
                if (value != null) {
                    entry.setValue(Double.parseDouble(value));
                }
                String counter = jedis.hget(
                        RedisSchema.summaryKey(sensorId, mt),
                        "counter"
                );
                if (counter != null) {
                    entry.setCounter(Long.parseLong(counter));
                }
                summary.addValue(mt, entry);
            });

        });


        return Optional.of(summary);
    }

    @Override
    public void handle(Data data) {
        try (Jedis jedis = jedisPool.getResource()) {
            if (!jedis.sismember(
                    RedisSchema.sensorKeys(),
                    String.valueOf(data.getSensorId())
            )) {
                jedis.sadd(
                        RedisSchema.sensorKeys(),
                        String.valueOf(data.getSensorId())
                );
            }
            updateMinValue(data, jedis);
            updateMaxValue(data, jedis);
            updateSumAndAvgValue(data, jedis);
        }
    }

    private void updateMinValue(Data data, Jedis jedis) {
        String key = RedisSchema.summaryKey(
                data.getSensorId(),
                data.getMeasurementType()
        );
        String value = jedis.hget(
                key,
                SummaryType.MIN.name().toLowerCase()
        );
        if (value == null || data.getMeasurement() < Double.parseDouble(value)) {
            jedis.hset(
                    key,
                    SummaryType.MIN.name().toLowerCase(),
                    String.valueOf(data.getMeasurement())
            );
        }

    }

    private void updateMaxValue(Data data, Jedis jedis) {
        String key = RedisSchema.summaryKey(
                data.getSensorId(),
                data.getMeasurementType()
        );
        String value = jedis.hget(
                key,
                SummaryType.MAX.name().toLowerCase()
        );
        if (value == null || data.getMeasurement() > Double.parseDouble(value)) {
            jedis.hset(
                    key,
                    SummaryType.MAX.name().toLowerCase(),
                    String.valueOf(data.getMeasurement())
            );
        }
    }

    private void updateSumAndAvgValue(Data data, Jedis jedis) {
        updateSumValue(data, jedis);
        String key = RedisSchema.summaryKey(
                data.getSensorId(),
                data.getMeasurementType()
        );
        String counter = jedis.hget(
                key,
                "counter"
        );
        if (counter == null) {
            counter = String.valueOf(
                    jedis.hset(
                            key,
                            "counter",
                            String.valueOf(1)
                    )
            );
        }else{
            counter = String.valueOf(
                    jedis.hincrBy(
                            key,
                            "counter",
                            1
                    )
            );
        }
        String sum = jedis.hget(
                key,
                SummaryType.SUM.name().toLowerCase()
        );
        String avg = String.valueOf(Double.parseDouble(sum)/Double.parseDouble(counter));
        jedis.hset(
                key,
                SummaryType.AVG.name().toLowerCase(),
                avg
        );

    }

    private void updateSumValue(Data data, Jedis jedis) {
        String key = RedisSchema.summaryKey(
                data.getSensorId(),
                data.getMeasurementType()
        );
        String value = jedis.hget(
                key,
                SummaryType.SUM.name().toLowerCase()
        );
        if (value == null) {
            jedis.hset(
                    key,
                    SummaryType.SUM.name().toLowerCase(),
                    String.valueOf(data.getMeasurement())
            );
        } else {
            jedis.hincrByFloat(
                    key,
                    SummaryType.SUM.name().toLowerCase(),
                    data.getMeasurement()
            );
        }
    }

}