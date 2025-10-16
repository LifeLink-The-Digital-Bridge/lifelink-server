package com.donorservice.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class MedicalDetailsDTO {
    private Long id;
    private UUID donorId;
    private Double hemoglobinLevel;
    private Double bloodGlucoseLevel;
    private Boolean hasDiabetes;
    private String bloodPressure;
    private Boolean hasDiseases;
    private Boolean takingMedication;
    private String diseaseDescription;
    private String currentMedications;
    private LocalDate lastMedicalCheckup;
    private String medicalHistory;
    private Boolean hasInfectiousDiseases;
    private String infectiousDiseaseDetails;
    private Double creatinineLevel;
    private String liverFunctionTests;
    private String cardiacStatus;
    private Double pulmonaryFunction;
    private String overallHealthStatus;
}