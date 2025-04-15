package com.donorservice.dto;

import com.donorservice.enums.DonorStatus;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class DonorDTO {
    private UUID id;
    private UUID userId;
    private LocalDate registrationDate;
    private DonorStatus status;
    private MedicalDetailsDTO medicalDetails;
    private EligibilityCriteriaDTO eligibilityCriteria;
    private ConsentFormDTO consentForm;

}
