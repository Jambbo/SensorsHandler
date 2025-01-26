package com.example.datageneratormicroservice.web.controller;

import com.example.datageneratormicroservice.model.Data;
import com.example.datageneratormicroservice.service.KafkaDataService;
import com.example.datageneratormicroservice.web.dto.DataDto;
import com.example.datageneratormicroservice.web.mapper.DataMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/data")
public class DataController {

    private final KafkaDataService kafkaDataService;

    private final DataMapper dataMapper;

    public void send(@RequestBody DataDto dataDto){
        Data data = dataMapper.toEntity(dataDto);
        kafkaDataService.send(data);
    }

}
