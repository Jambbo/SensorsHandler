package com.example.datastoremicroservice.service.threshold;

import com.example.datastoremicroservice.model.Data;
import com.example.datastoremicroservice.model.MeasurementType;
import com.example.datastoremicroservice.model.Notification;
import com.example.datastoremicroservice.model.Threshold;
import com.example.datastoremicroservice.repository.threhsold.ThresholdRepository;
import com.example.datastoremicroservice.service.kafka.KafkaDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ThresholdServiceImpl implements ThresholdService {
    private final ThresholdRepository thresholdRepository;
    private final KafkaDataService kafkaDataService;

    @Override
    public void setThreshold(Long sensorId, Threshold threshold) {
        threshold.setSensorId(sensorId);
        thresholdRepository.setThreshold(threshold);
    }

    @Override
    public Threshold getThreshold(Long sensorId, MeasurementType measurementType) {
        return thresholdRepository.getThreshold(sensorId, measurementType).orElseThrow();
    }

    @Override
    public void check(Data data) {
        Optional<Threshold> thresholdOpt = thresholdRepository.getThreshold(data.getSensorId(), data.getMeasurementType());

        if (thresholdOpt.isEmpty()) return;
        Threshold threshold = thresholdOpt.get();

        Notification notification = new Notification();
        if (threshold.getMinMeasurement() > data.getMeasurement()) {
            notification.setMessage(String.format(
                    "The value of %s in sensor %d = %.2f is below threshold (%.2f)",
                    data.getMeasurementType(), data.getSensorId(),
                    data.getMeasurement(), threshold.getMinMeasurement()
            ));
            kafkaDataService.send(notification);
        }
        if (threshold.getMaxMeasurement() < data.getMeasurement()) {
            notification.setMessage(String.format(
                    "The value of %s in sensor %d = %.2f is above threshold (%.2f)",
                    data.getMeasurementType(), data.getSensorId(),
                    data.getMeasurement(), threshold.getMinMeasurement()
            ));
            kafkaDataService.send(notification);
        }

    }
}
