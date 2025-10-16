package com.donorservice.dto;

import com.donorservice.enums.SmokingStatus;
import com.donorservice.enums.AlcoholStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class EligibilityCriteriaDTO {
    private Long id;
    private UUID donorId;
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
    private Double height;
    private Double bodyMassIndex;
    private String bodySize;
    private Boolean isLivingDonor;

    private SmokingStatus smokingStatus;
    private Integer packYears;
    private LocalDate quitSmokingDate;

    private AlcoholStatus alcoholStatus;
    private Integer drinksPerWeek;
    private LocalDate quitAlcoholDate;
    private Integer alcoholAbstinenceMonths;
}
