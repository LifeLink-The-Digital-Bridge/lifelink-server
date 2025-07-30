package com.matchingservice.kafka;

import com.matchingservice.kafka.event.DonorEvent;
import com.matchingservice.kafka.event.DonationEvent;
import com.matchingservice.kafka.event.LocationEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class EventConsumer {

    @KafkaListener(topics = "donor-events", groupId = "matching-service-group", containerFactory = "donorKafkaListenerFactory")
    public void consumeDonorEvent(DonorEvent event) {
        System.out.println("Received DonorEvent: " + event);
    }

    @KafkaListener(topics = "donation-events", groupId = "matching-service-group", containerFactory = "donationKafkaListenerFactory")
    public void consumeDonationEvent(DonationEvent event) {
        System.out.println("Received DonationEvent: " + event);
    }

    @KafkaListener(topics = "location-events", groupId = "matching-service-group", containerFactory = "locationKafkaListenerFactory")
    public void consumeLocationEvent(LocationEvent event) {
        System.out.println("Received LocationEvent: " + event);
    }
}
