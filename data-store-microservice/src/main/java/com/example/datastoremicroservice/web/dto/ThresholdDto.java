package com.example.datastoremicroservice.web.dto;

import com.example.datastoremicroservice.model.MeasurementType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Getter
@Setter
@ToString
public class ThresholdDto {

    private MeasurementType measurementType;
    private double minMeasurement;
    private double maxMeasurement;


}
