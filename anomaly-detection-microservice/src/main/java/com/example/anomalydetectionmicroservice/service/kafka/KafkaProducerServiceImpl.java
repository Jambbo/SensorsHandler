package com.example.anomalydetectionmicroservice.service.kafka;

import com.example.anomalydetectionmicroservice.model.AnomalyEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@Service
@RequiredArgsConstructor
public class KafkaProducerServiceImpl implements KafkaProducerService {
    private final KafkaSender<String, Object> kafkaSender;

    @Override
    public void send(AnomalyEvent event) {
        String topic = "anomaly-detected";
        kafkaSender.send(
                Mono.just(
                        SenderRecord.create(
                                topic,
                                null,
                                System.currentTimeMillis(),
                                String.valueOf(event.hashCode()),
                                event,
                                null
                        )
                )
        ).subscribe();
    }


}
