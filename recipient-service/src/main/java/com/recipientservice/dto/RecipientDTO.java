package com.recipientservice.dto;

import com.recipientservice.enums.Availability;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class RecipientDTO {
    private UUID id;
    private UUID userId;
    private Availability availability;
    private MedicalDetailsDTO medicalDetails;
    private EligibilityCriteriaDTO eligibilityCriteria;
    private ConsentFormDTO consentForm;
    private List<LocationDTO> addresses;
    private HLAProfileDTO hlaProfile;
}

