package com.donorservice.kafka;

import com.donorservice.kafka.event.DonorEvent;
import com.donorservice.kafka.event.DonationEvent;
import com.donorservice.kafka.event.HLAProfileEvent;
import com.donorservice.kafka.event.LocationEvent;
import com.donorservice.kafka.event.DonationCancelledEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventPublisher {

    private final KafkaTemplate<String, DonorEvent> donorKafkaTemplate;
    private final KafkaTemplate<String, DonationEvent> donationKafkaTemplate;
    private final KafkaTemplate<String, LocationEvent> locationKafkaTemplate;
    private final KafkaTemplate<String, HLAProfileEvent> hlaProfileKafkaTemplate;
    private final KafkaTemplate<String, DonationCancelledEvent> donationCancelledKafkaTemplate;

    public void publishDonorEvent(DonorEvent event) {
        donorKafkaTemplate.send("donor-events", event.getDonorId().toString(), event);
    }

    public void publishDonationEvent(DonationEvent event) {
        donationKafkaTemplate.send("donation-events", event.getDonorId().toString(), event);
    }

    public void publishLocationEvent(LocationEvent event) {
        locationKafkaTemplate.send("donor-location-events", event.getDonorId().toString(), event);
    }

    public void publishHLAProfileEvent(HLAProfileEvent event) {
        hlaProfileKafkaTemplate.send("donor-hla-profile-event", event.getDonorId().toString(), event);
    }

    public void publishDonationCancelledEvent(DonationCancelledEvent event) {
        donationCancelledKafkaTemplate.send("donation-cancelled", event.getDonationId().toString(), event);
        System.out.println("Published donation cancelled event for donation: " + event.getDonationId());
    }
}
