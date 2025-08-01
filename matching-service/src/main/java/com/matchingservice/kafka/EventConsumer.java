package com.matchingservice.kafka;

import com.matchingservice.kafka.event.donor_events.DonorEvent;
import com.matchingservice.kafka.event.donor_events.DonationEvent;
import com.matchingservice.kafka.event.donor_events.DonorLocationEvent;
import com.matchingservice.kafka.event.recipient_events.RecipientEvent;
import com.matchingservice.kafka.event.recipient_events.ReceiveRequestEvent;
import com.matchingservice.kafka.event.recipient_events.RecipientLocationEvent;
import com.matchingservice.service.MatchingEventHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventConsumer {

    private final MatchingEventHandlerService handler;

    @KafkaListener(topics = "donor-events", groupId = "matching-service-group", containerFactory = "donorKafkaListenerFactory")
    public void consumeDonorEvent(DonorEvent event) {
        System.out.println("Received DonorEvent: " + event);
        handler.handleDonorEvent(event);
    }

    @KafkaListener(topics = "donation-events", groupId = "matching-service-group", containerFactory = "donationKafkaListenerFactory")
    public void consumeDonationEvent(DonationEvent event) {
        System.out.println("Received DonationEvent: " + event);
        handler.handleDonationEvent(event);
    }

    @KafkaListener(topics = "donor-location-events", groupId = "matching-service-group", containerFactory = "donorLocationKafkaListenerFactory")
    public void consumeDonorLocationEvent(DonorLocationEvent event) {
        System.out.println("Received DonorLocationEvent: " + event);
         handler.handleLocationEvent(event);
    }

    @KafkaListener(topics = "recipient-events", groupId = "matching-service-group", containerFactory = "recipientKafkaListenerFactory")
    public void consumeRecipientEvent(RecipientEvent event) {
        System.out.println("Received RecipientEvent: " + event);
    }

    @KafkaListener(topics = "receive-request-events", groupId = "matching-service-group", containerFactory = "receiveRequestKafkaListenerFactory")
    public void consumeReceiveRequestEvent(ReceiveRequestEvent event) {
        System.out.println("Received ReceiveRequestEvent: " + event);
    }

    @KafkaListener(topics = "recipient-location-events", groupId = "matching-service-group", containerFactory = "recipientLocationKafkaListenerFactory")
    public void consumeRecipientLocationEvent(RecipientLocationEvent event) {
        System.out.println("Received RecipientLocationEvent: " + event);
    }
}
