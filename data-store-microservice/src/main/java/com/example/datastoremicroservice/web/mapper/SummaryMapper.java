package com.example.datastoremicroservice.web.mapper;

import com.example.datastoremicroservice.model.Summary;
import com.example.datastoremicroservice.web.dto.SummaryDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SummaryMapper extends Mappable<Summary, SummaryDto> {
}
