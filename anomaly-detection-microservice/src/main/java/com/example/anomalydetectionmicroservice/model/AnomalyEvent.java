package com.example.anomalydetectionmicroservice.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

//what gets published to kafka
@NoArgsConstructor
@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnomalyEvent {

    Long sensorId;
    LocalDateTime timestamp;
    double measurement;
    MeasurementType measurementType;
    double zScore;
    double mean;
    double stdDev;
}
