package com.example.datastoremicroservice.service.kafka;

import com.example.datastoremicroservice.model.Data;
import com.example.datastoremicroservice.model.Notification;
import com.example.datastoremicroservice.service.kafka.KafkaDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@Service
@RequiredArgsConstructor
public class KafkaDataServiceImpl implements KafkaDataService {

    private final KafkaSender<String, Object> sender;

    @Override
    public void send(Notification notification) {
        String topic = "threshold-exceed";
        sender.send(
                        Mono.just(
                                SenderRecord.create(
                                        topic,
                                        null,
                                        System.currentTimeMillis(),
                                        String.valueOf(notification.hashCode()),//key for message
                                        notification.getMessage(),
                                        null
                                )
                        )
                )
                .subscribe();

    }
}
