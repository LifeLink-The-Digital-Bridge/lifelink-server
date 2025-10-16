package com.matchingservice.kafka.event.recipient_events;

import lombok.Data;

import java.util.UUID;

@Data
public class RecipientLocationEvent {
    private UUID locationId;
    private UUID recipientId;
    private String addressLine;
    private String landmark;
    private String area;
    private String city;
    private String district;
    private String state;
    private String country;
    private String pincode;
    private Double latitude;
    private Double longitude;
}
