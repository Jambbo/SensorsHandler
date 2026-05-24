package com.example.datastoremicroservice.repository.threhsold;

import com.example.datastoremicroservice.model.MeasurementType;
import com.example.datastoremicroservice.model.Threshold;

import java.util.Optional;

public interface ThresholdRepository {

    void setThreshold(Threshold threshold);
    Optional<Threshold> getThreshold(Long sensorId, MeasurementType measurementType);

}
