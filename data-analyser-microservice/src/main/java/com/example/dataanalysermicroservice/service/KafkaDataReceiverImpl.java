package com.example.dataanalysermicroservice.service;

import com.example.dataanalysermicroservice.config.LocalDateTimeDeserializer;
import com.example.dataanalysermicroservice.model.Data;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.kafka.receiver.KafkaReceiver;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class KafkaDataReceiverImpl implements KafkaDataReceiver{

    private final KafkaReceiver<String, Object> receiver;
    private final LocalDateTimeDeserializer localDateTimeDeserializer;
    private final KafkaDataService kafkaDataService;

    @PostConstruct
    private void init(){
        fetch();
    }


    @Override
    public void fetch() {
        Gson gson = new GsonBuilder()
                //register adapter to specify how to handle localdatetime using own implementation of ldtd
                .registerTypeAdapter(LocalDateTime.class,
                        localDateTimeDeserializer)
                .create();
        receiver.receive()
                .subscribe(r -> {
                    Data data = gson
                            .fromJson(r.value().toString(), Data.class);//retrieve value(message) from kafka
                    kafkaDataService.handle(data);
                    //specify that we already obtained the message and move to the next one
                    r.receiverOffset().acknowledge();
                });
    }


}
