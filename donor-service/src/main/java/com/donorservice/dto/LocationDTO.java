package com.donorservice.dto;

import lombok.Data;

@Data
public class LocationDTO {
    private Long id;
    private String city;
    private String state;
    private String country;
    private String pincode;
}



