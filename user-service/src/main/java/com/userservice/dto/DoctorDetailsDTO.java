package com.userservice.dto;

import lombok.Data;

@Data
public class DoctorDetailsDTO {
    private String medicalRegistrationNumber;
    private String specialization;
    private String qualification;
    private String hospitalName;
    private String clinicAddress;
    private Integer yearsOfExperience;
    private Double consultationFee;
    private Double latitude;
    private Double longitude;
}
