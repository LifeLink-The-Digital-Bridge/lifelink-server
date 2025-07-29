package com.donorservice.kafka;

import com.donorservice.kafka.event.DonorEvent;
import com.donorservice.kafka.event.DonationEvent;
import com.donorservice.kafka.event.LocationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventPublisher {

    private final KafkaTemplate<String, DonorEvent> donorKafkaTemplate;
    private final KafkaTemplate<String, DonationEvent> donationKafkaTemplate;
    private final KafkaTemplate<String, LocationEvent> locationKafkaTemplate;

    public void publishDonorEvent(DonorEvent event) {
        donorKafkaTemplate.send("donor-events", event.getDonorId().toString(), event);
    }

    public void publishDonationEvent(DonationEvent event) {
        donationKafkaTemplate.send("donation-events", event.getDonationId().toString(), event);
    }

    public void publishLocationEvent(LocationEvent event) {
        locationKafkaTemplate.send("location-events", event.getLocationId().toString(), event);
    }
}
