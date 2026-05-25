package com.example.anomalydetectionmicroservice.service.anomaly;

import com.example.anomalydetectionmicroservice.model.SensorReading;

public interface AnomalyDetectionService {

    void analyze(SensorReading reading);

}
