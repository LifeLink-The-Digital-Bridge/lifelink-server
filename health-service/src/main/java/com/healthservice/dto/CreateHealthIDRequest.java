package com.healthservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateHealthIDRequest {
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @NotBlank(message = "Blood group is required")
    private String bloodGroup;
    
    private String rhFactor;
    private String allergies;
    private String chronicConditions;
    private String emergencyContactName;
    private String emergencyContactPhone;
    
    @NotBlank(message = "Emergency PIN is required")
    private String emergencyPin;
    
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
}
