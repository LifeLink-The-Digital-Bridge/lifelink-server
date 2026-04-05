package com.healthservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MigrantProfileDTO {
    
    private UUID id;
    private UUID userId;
    private String healthId;
    private String bloodGroup;
    private Double heightCm;
    private Double weightKg;
    private String allergies;
    private String chronicConditions;
    private String currentMedications;
    private String vaccinationStatus;
    private String healthRiskScore;
    private LocalDateTime lastCheckupDate;
}
