package com.example.anomalydetectionmicroservice.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

//data deserialized from kafka
@NoArgsConstructor
@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SensorReading {

    Long sensorId;
    MeasurementType measurementType;
    double measurement;

}
