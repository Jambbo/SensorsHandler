package com.example.datastoremicroservice.service.debezium;

public interface CDCEventConsumer {

    void handle(String message);

}
