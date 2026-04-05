package com.healthservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonorEnrollmentRequest {
    
    private UUID userId;
    private String bloodGroup;
    private String rhFactor;
    private String medicalHistory;
    private String currentMedications;
    private Boolean hasChronicDiseases;
    private Boolean hasDiabetes;
    private String bloodPressure;
    private Double hemoglobinLevel;
    private LocalDateTime lastCheckupDate;
    private String currentCity;
    private String currentState;
    private Boolean willingToDonateBlood;
    private Boolean willingToDonateOrgan;
    private Boolean consentGiven;
}
