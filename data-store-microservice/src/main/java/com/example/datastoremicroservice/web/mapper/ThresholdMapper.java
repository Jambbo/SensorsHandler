package com.example.datastoremicroservice.web.mapper;

import com.example.datastoremicroservice.model.Threshold;
import com.example.datastoremicroservice.web.dto.ThresholdDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ThresholdMapper extends Mappable<Threshold, ThresholdDto>{
}
