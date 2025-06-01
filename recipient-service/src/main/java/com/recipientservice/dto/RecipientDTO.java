package com.recipientservice.dto;

import com.recipientservice.enums.Availability;
import lombok.Data;

import java.util.UUID;

@Data
public class RecipientDTO {
    private UUID id;
    private UUID userId;
    private Availability availability;
    private LocationDTO location;
    private MedicalDetailsDTO medicalDetails;
    private EligibilityCriteriaDTO eligibilityCriteria;
    private ConsentFormDTO consentForm;
}

