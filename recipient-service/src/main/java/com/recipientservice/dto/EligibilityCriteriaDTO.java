package com.recipientservice.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EligibilityCriteriaDTO {
    private Long id;
    private Boolean medicallyEligible;
    private Boolean legalClearance;
    private String notes;
    private LocalDate lastReviewed;
}
