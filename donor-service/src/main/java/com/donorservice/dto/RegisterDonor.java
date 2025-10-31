package com.donorservice.dto;

import com.donorservice.enums.DonorStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class RegisterDonor {
    private LocalDate registrationDate;
    private DonorStatus status;
    private MedicalDetailsDTO medicalDetails;
    private EligibilityCriteriaDTO eligibilityCriteria;
    private ConsentFormDTO consentForm;
    private List<LocationDTO> addresses;
    private HLAProfileDTO hlaProfile;

    public RegisterDonor() {
        this.registrationDate = LocalDate.now();
        this.status = DonorStatus.ACTIVE;
    }
}
