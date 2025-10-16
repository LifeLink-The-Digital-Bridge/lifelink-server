package com.recipientservice.dto;

import com.recipientservice.enums.SmokingStatus;
import com.recipientservice.enums.AlcoholStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class EligibilityCriteriaDTO {
    private Long id;
    private UUID recipientId;
    private Boolean ageEligible;
    private Integer age;
    private LocalDate dob;
    private Boolean weightEligible;
    private Double weight;
    private Boolean medicallyEligible;
    private Boolean legalClearance;
    private String notes;
    private LocalDate lastReviewed;
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
