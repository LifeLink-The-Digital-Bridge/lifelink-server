package com.donorservice.dto;

import com.donorservice.enums.DonorStatus;
import jakarta.persistence.PrePersist;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class RegisterDonor {
    private LocalDate registrationDate;
    private DonorStatus status;
    private MedicalDetailsDTO medicalDetails;
    private EligibilityCriteriaDTO eligibilityCriteria;
    private ConsentFormDTO consentForm;
    private LocationDTO location;

    @PrePersist
    public void setRegistrationDate() {
        if (registrationDate == null) {
            registrationDate = LocalDate.now();
        }
    }

}

