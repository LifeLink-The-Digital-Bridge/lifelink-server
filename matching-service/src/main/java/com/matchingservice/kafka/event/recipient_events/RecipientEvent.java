package com.matchingservice.kafka.event.recipient_events;

import com.matchingservice.enums.Availability;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class RecipientEvent {
    private UUID recipientId;
    private UUID userId;
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
