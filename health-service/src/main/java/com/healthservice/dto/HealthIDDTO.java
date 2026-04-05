package com.healthservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthIDDTO {
    
    private UUID id;
    private UUID userId;
    private String healthId;
    private String qrCodeData;
    private String bloodGroup;
    private String rhFactor;
    private String allergies;
    private String chronicConditions;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private Double heightCm;
    private Double weightKg;
    private String currentMedications;
    private String vaccinationStatus;
    private String medicalHistory;
    private Boolean hasChronicDiseases;
    private Boolean hasDiabetes;
    private String bloodPressure;
    private Double hemoglobinLevel;
    private LocalDateTime lastCheckupDate;
    private String currentCity;
    private String currentState;
    private String occupation;
    private String preferredLanguage;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
