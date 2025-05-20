package com.donorservice.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EligibilityCriteriaDTO {
    private Long id;
    private Boolean ageEligible;
    private Integer age;
    private LocalDate dob;
    private Boolean weightEligible;
    private Double weight;
    private Boolean medicalClearance;
    private Boolean recentTattooOrPiercing;
    private String recentTravelDetails;
    private Boolean recentVaccination;
    private Boolean recentSurgery;
    private String chronicDiseases;
    private String allergies;
    private LocalDate lastDonationDate;
}
