package com.matchingservice.kafka.event.donor_events;

import lombok.Data;

import java.util.UUID;

@Data
public class DonorLocationEvent {
    private UUID locationId;
    private UUID donorId;
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

