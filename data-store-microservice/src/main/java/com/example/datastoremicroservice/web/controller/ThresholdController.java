package com.example.datastoremicroservice.web.controller;


import com.example.datastoremicroservice.model.MeasurementType;
import com.example.datastoremicroservice.model.Threshold;
import com.example.datastoremicroservice.service.threshold.ThresholdService;
import com.example.datastoremicroservice.web.dto.ThresholdDto;
import com.example.datastoremicroservice.web.mapper.ThresholdMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/thresholds")
public class ThresholdController {
    private final ThresholdService thresholdService;
    private final ThresholdMapper thresholdMapper;

    @PostMapping("{sensorId}")
    public ResponseEntity<Void> setThreshold(
            @PathVariable Long sensorId,
            @RequestBody ThresholdDto thresholdDto
    ) {
        Threshold threshold = thresholdMapper.toEntity(thresholdDto);
        thresholdService.setThreshold(sensorId, threshold);
        return ResponseEntity.ok().build();
    }

    @GetMapping("{sensorId}")
    public ResponseEntity<ThresholdDto> getThreshold(
            @PathVariable Long sensorId,
            @RequestParam MeasurementType measurementType
    ) {
        Threshold threshold = thresholdService.getThreshold(sensorId, measurementType);
        ThresholdDto thresholdDto = thresholdMapper.toDto(threshold);
        return ResponseEntity.ok(thresholdDto);
    }

}
