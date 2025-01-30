package com.example.dataanalysermicroservice.service;

import com.example.dataanalysermicroservice.model.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaDataServiceImpl implements KafkaDataService{

    @Override
    public void handle(Data data) {
        System.out.println("Data object is received: "+data.toString());
    }

}
