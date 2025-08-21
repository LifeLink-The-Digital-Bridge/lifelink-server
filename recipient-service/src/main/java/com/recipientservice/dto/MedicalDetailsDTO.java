package com.recipientservice.dto;


import lombok.Data;

import java.util.UUID;

@Data
public class MedicalDetailsDTO {
    private Long id;
    private UUID recipientId;
    private Double hemoglobinLevel;
    private String bloodPressure;
    private String diagnosis;
    private String allergies;
    private String currentMedications;
    private String additionalNotes;
    private Boolean hasInfectiousDiseases;
    private String infectiousDiseaseDetails;
    private Double creatinineLevel;
    private String liverFunctionTests;
    private String cardiacStatus;
    private Double pulmonaryFunction;
    private String overallHealthStatus;
}
