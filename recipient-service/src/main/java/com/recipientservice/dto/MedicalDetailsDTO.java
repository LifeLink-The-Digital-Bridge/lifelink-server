package com.recipientservice.dto;


import lombok.Data;

@Data
public class MedicalDetailsDTO {
    private Long id;
    private String diagnosis;
    private String allergies;
    private String currentMedications;
    private String additionalNotes;
}