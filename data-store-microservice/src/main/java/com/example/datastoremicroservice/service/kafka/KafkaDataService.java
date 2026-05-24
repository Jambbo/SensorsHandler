package com.example.datastoremicroservice.service.kafka;

import com.example.datastoremicroservice.model.Notification;

public interface KafkaDataService {

    void send(Notification notification);

}
