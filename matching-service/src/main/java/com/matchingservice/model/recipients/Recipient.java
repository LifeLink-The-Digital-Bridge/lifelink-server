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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private UUID recipientId;

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    private Availability availability;

    private Long medicalDetailsId;
    private String diagnosis;
    private String allergies;
    private String currentMedications;
    private String additionalNotes;

    private Long eligibilityCriteriaId;
    private Boolean medicallyEligible;
    private Boolean legalClearance;
    private String eligibilityNotes;
    private LocalDate lastReviewed;
}
