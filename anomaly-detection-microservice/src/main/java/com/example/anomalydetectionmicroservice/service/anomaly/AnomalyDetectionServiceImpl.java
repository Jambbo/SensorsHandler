package com.example.anomalydetectionmicroservice.service.anomaly;

import com.example.anomalydetectionmicroservice.model.AnomalyEvent;
import com.example.anomalydetectionmicroservice.model.SensorReading;
import com.example.anomalydetectionmicroservice.repository.RollingStatsRepository;
import com.example.anomalydetectionmicroservice.service.kafka.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnomalyDetectionServiceImpl implements AnomalyDetectionService {

    private final RollingStatsRepository rollingStatsRepository;
    private final KafkaProducerService kafkaProducerService;

    private static final double Z_SCORE_THRESHOLD = 2.5;
    private static final int WINDOW_SIZE = 20; // last 20 readings per sensor/type

    @Override
    public void analyze(SensorReading reading) {
        rollingStatsRepository.addReading(
                reading.getSensorId(),
                reading.getMeasurementType(),
                reading.getMeasurement()
        );

        List<Double> window = rollingStatsRepository.getWindow(
                reading.getSensorId(),
                reading.getMeasurementType(),
                WINDOW_SIZE
        );

        // need at least a few readings before we can detect anomalies
        if (window.size() < 5) return;

        double mean = calculateMean(window);
        double stdDev = calculateStdDev(window, mean);

        if (stdDev == 0) return;

        double zScore = Math.abs((reading.getMeasurement() - mean) / stdDev);

        if (zScore > Z_SCORE_THRESHOLD) {
            AnomalyEvent event = new AnomalyEvent();
            event.setSensorId(reading.getSensorId());
            event.setMeasurementType(reading.getMeasurementType());
            event.setMeasurement(reading.getMeasurement());
            event.setZScore(zScore);
            event.setMean(mean);
            event.setStdDev(stdDev);
            event.setTimestamp(LocalDateTime.now());
            kafkaProducerService.send(event);
        }

    }


    private double calculateMean(List<Double> window) {
        return window.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }


    private double calculateStdDev(List<Double> window, double mean) {
        return Math.sqrt(
                window.stream()
                        .mapToDouble(v -> Math.pow(v - mean, 2))
                        .average()
                        .orElse(0.0)
        );
    }

}
