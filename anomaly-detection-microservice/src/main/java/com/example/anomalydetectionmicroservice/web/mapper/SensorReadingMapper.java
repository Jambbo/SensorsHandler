package com.example.anomalydetectionmicroservice.web.mapper;

import com.example.anomalydetectionmicroservice.model.MeasurementType;
import com.example.anomalydetectionmicroservice.model.SensorReading;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

@Component
public class SensorReadingMapper {

    public SensorReading fromDebeziumPayload(String message) {
        JsonObject payload = JsonParser.parseString(message)
                .getAsJsonObject()
                .get("payload")
                .getAsJsonObject();
        return fromPayload(payload);
    }

    private SensorReading fromPayload(JsonObject payload) {
        SensorReading reading = new SensorReading();
        reading.setSensorId(payload.get("sensor_id").getAsLong());
        reading.setMeasurement(payload.get("measurement").getAsDouble());
        reading.setMeasurementType(
                MeasurementType.valueOf(payload.get("type").getAsString())
        );
        reading.setTimestamp(
                LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(payload.get("timestamp").getAsLong() / 1000),
                        TimeZone.getDefault().toZoneId()
                )
        );
        return reading;
    }

}
