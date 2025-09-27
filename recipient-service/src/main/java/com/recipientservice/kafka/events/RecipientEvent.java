package com.recipientservice.kafka.events;

import com.recipientservice.enums.AlcoholStatus;
import com.recipientservice.enums.Availability;
import com.recipientservice.enums.SmokingStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class RecipientEvent {
    private UUID recipientId;
    private UUID userId;
    private Availability availability;

    private Long medicalDetailsId;
    private Double hemoglobinLevel;
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

    private Long eligibilityCriteriaId;
    private Boolean ageEligible;
    private Integer age;
    private LocalDate dob;
    private Boolean weightEligible;
    private Double weight;
    private Boolean medicallyEligible;
    private Boolean legalClearance;
    private String eligibilityNotes;
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
