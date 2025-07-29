package com.donorservice.kafka.event;

import lombok.Data;

import java.util.UUID;

@Data
public class LocationEvent {
    private Long locationId;
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
