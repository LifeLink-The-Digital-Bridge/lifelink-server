package com.matchingservice.dto;

import com.matchingservice.enums.Availability;
import com.matchingservice.enums.AlcoholStatus;
import com.matchingservice.enums.SmokingStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class RecipientDTO {
    private UUID recipientId;
    private UUID userId;
    private Availability availability;

    private RecipientMedicalDetailsDTO medicalDetails;
    private RecipientEligibilityCriteriaDTO eligibilityCriteria;
    private HLAProfileDTO hlaProfile;
    private List<LocationDTO> locations;

    @Data
    public static class RecipientMedicalDetailsDTO {
        private Long medicalDetailsId;
        private Double hemoglobinLevel;
        private Double bloodGlucoseLevel;
        private Boolean hasDiabetes;
        private String bloodPressure;
        private String diagnosis;
        private String allergies;
        private String currentMedications;
        private String additionalNotes;
        private Boolean hasInfectiousDiseases;
        private String infectiousDiseaseDetails;
        private Double creatinineLevel;
        private String liverFunctionTests;
        private String cardiacStatus;
        private Double pulmonaryFunction;
        private String overallHealthStatus;
    }

    @Data
    public static class RecipientEligibilityCriteriaDTO {
        private Long eligibilityCriteriaId;
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
}
