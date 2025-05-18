package com.donorservice.dto;

import lombok.Data;

@Data
public class MedicalDetailsDTO {
    private Long id;
    private Double hemoglobinLevel;
    private String bloodPressure;
    private Boolean hasDiseases;
    private Boolean takingMedication;
    private String diseaseDescription;
    private Boolean recentlyIll;
    private Boolean isPregnant;
}

