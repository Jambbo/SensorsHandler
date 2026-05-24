package com.example.datastoremicroservice.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class Notification {

    private Long sensorId;
    private MeasurementType measurementType;
    private String message;
    private double maxValue;
    private double minValue;
    private LocalDateTime timestamp;

}
