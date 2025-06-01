package com.recipientservice.dto;

import com.recipientservice.enums.Availability;
import com.recipientservice.enums.BloodType;
import com.recipientservice.enums.UrgencyLevel;
import lombok.Data;

@Data
public class RegisterRecipientDTO {
    private Availability availability;
    private BloodType requiredBloodType;
    private String organNeeded;
    private UrgencyLevel urgencyLevel;
    private LocationDTO location;
    private MedicalDetailsDTO medicalDetails;
    private EligibilityCriteriaDTO eligibilityCriteria;
    private ConsentFormDTO consentForm;
}
