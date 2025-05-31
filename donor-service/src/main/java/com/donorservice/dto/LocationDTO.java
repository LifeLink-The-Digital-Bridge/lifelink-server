package com.donorservice.dto;

import lombok.Data;

@Data
public class LocationDTO {
    private Long id;
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
