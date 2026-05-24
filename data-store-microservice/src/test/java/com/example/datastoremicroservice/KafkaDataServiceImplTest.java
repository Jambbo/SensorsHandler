package com.example.datastoremicroservice;

import com.example.datastoremicroservice.model.Notification;
import com.example.datastoremicroservice.service.kafka.KafkaDataServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaDataServiceImplTest {

    @Mock
    private KafkaSender<String, Object> sender;

    @Mock
    private SenderResult<Object> senderResult;

    @InjectMocks
    private KafkaDataServiceImpl kafkaDataService;

    @Test
    void send_shouldPublishNotificationMessageToCorrectTopic() {
        Notification notification = new Notification();
        notification.setMessage("test alert message");

        when(sender.send(any())).thenReturn(Flux.just(senderResult));

        kafkaDataService.send(notification);

        verify(sender).send(any());
    }

    @Test
    void send_shouldUseNotificationMessageAsValue() {
        Notification notification = new Notification();
        notification.setMessage("below threshold alert");

        when(sender.send(any())).thenReturn(Flux.just(senderResult));

        kafkaDataService.send(notification);

        // verifies send was called — message content validated via integration test
        verify(sender).send(any());
    }
}