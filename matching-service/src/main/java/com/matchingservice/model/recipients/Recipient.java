package com.matchingservice.model.recipients;

import com.matchingservice.enums.Availability;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "recipient")
public class Recipient {

    @Id
    private UUID recipientId;

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
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
}
