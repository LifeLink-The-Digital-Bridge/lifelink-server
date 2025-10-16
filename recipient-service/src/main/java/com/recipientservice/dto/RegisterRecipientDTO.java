package com.recipientservice.dto;

import com.recipientservice.enums.Availability;
import lombok.Data;

import java.util.List;

@Data
public class RegisterRecipientDTO {
    private Availability availability;
    private List<LocationDTO> addresses;
    private MedicalDetailsDTO medicalDetails;
    private EligibilityCriteriaDTO eligibilityCriteria;
    private ConsentFormDTO consentForm;
    private HLAProfileDTO hlaProfile;
}
