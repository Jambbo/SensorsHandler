package com.example.datastoremicroservice.model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class Threshold {
    private Long sensorId;
    private MeasurementType measurementType;
    private double minMeasurement;
    private double maxMeasurement;


}
