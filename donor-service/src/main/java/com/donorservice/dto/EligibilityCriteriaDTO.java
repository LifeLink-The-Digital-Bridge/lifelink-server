package com.donorservice.dto;

import lombok.Data;

@Data
public class EligibilityCriteriaDTO {
    private Long id;
    private Boolean ageEligible;
    private Boolean weightEligible;
    private Boolean medicalClearance;
    private Boolean recentTattooOrPiercing;
    private Boolean recentTravel;
}
