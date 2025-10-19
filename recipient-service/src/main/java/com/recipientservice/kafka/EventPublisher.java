package com.recipientservice.kafka;

import com.recipientservice.kafka.events.*;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventPublisher {

    private final KafkaTemplate<String, RecipientEvent> recipientKafkaTemplate;
    private final KafkaTemplate<String, ReceiveRequestEvent> receiveRequestKafkaTemplate;
    private final KafkaTemplate<String, LocationEvent> recipientLocationKafkaTemplate;
    private final KafkaTemplate<String, HLAProfileEvent> hlaProfileKafkaTemplate;
    private final KafkaTemplate<String, RequestCancelledEvent> requestCancelledKafkaTemplate;

    public void publishRecipientEvent(RecipientEvent event) {
        recipientKafkaTemplate.send("recipient-events", event.getRecipientId().toString(), event);
    }

    public void publishReceiveRequestEvent(ReceiveRequestEvent event) {
        receiveRequestKafkaTemplate.send("receive-request-events", event.getRecipientId().toString(), event);
    }

    public void publishRecipientLocationEvent(LocationEvent event) {
        recipientLocationKafkaTemplate.send("recipient-location-events", event.getRecipientId().toString(), event);
    }

    public void publishHLAProfileEvent(HLAProfileEvent event) {
        hlaProfileKafkaTemplate.send("recipient-hla-profile-event", event.getRecipientId().toString(), event);
    }

    public void publishRequestCancelledEvent(RequestCancelledEvent event) {
        requestCancelledKafkaTemplate.send("request-cancelled", event.getRequestId().toString(), event);
        System.out.println("Published request cancelled event for request: " + event.getRequestId());
    }
}
