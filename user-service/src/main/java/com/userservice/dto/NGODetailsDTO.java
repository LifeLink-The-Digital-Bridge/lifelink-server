package com.userservice.dto;

import lombok.Data;

@Data
public class NGODetailsDTO {
    private String organizationName;
    private String registrationNumber;
    private Integer registrationYear;
    private String organizationType;
    private String serviceAreas;
    private String headOfficeAddress;
    private String website;
    private Integer totalVolunteers;
    private Double latitude;
    private Double longitude;
}
