package com.example.datastoremicroservice.service.threshold;

import com.example.datastoremicroservice.model.Data;
import com.example.datastoremicroservice.model.MeasurementType;
import com.example.datastoremicroservice.model.Threshold;
import com.example.datastoremicroservice.web.dto.ThresholdDto;

import java.util.Set;

public interface ThresholdService {

    void setThreshold(Long sensorId, Threshold threshold);

    Threshold getThreshold(Long sensorId, MeasurementType measurementType);

    void check(Data data);
}
