package com.recipientservice.kafka;

import com.recipientservice.kafka.events.RecipientEvent;
import com.recipientservice.kafka.events.ReceiveRequestEvent;
import com.recipientservice.kafka.events.LocationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventPublisher {

    private final KafkaTemplate<String, RecipientEvent> recipientKafkaTemplate;
    private final KafkaTemplate<String, ReceiveRequestEvent> receiveRequestKafkaTemplate;
    private final KafkaTemplate<String, LocationEvent> recipientLocationKafkaTemplate;

    public void publishRecipientEvent(RecipientEvent event) {
        recipientKafkaTemplate.send("recipient-events", event.getRecipientId().toString(), event);
    }

    public void publishReceiveRequestEvent(ReceiveRequestEvent event) {
        receiveRequestKafkaTemplate.send("receive-request-events", event.getReceiveRequestId().toString(), event);
    }

    public void publishRecipientLocationEvent(LocationEvent event) {
        recipientLocationKafkaTemplate.send("recipient-location-events", event.getLocationId().toString(), event);
    }
}
