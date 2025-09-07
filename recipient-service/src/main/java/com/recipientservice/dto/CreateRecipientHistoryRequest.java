package com.recipientservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateRecipientHistoryRequest {
    private UUID matchId;
    private UUID donationId;
    private UUID donorUserId;
    private LocalDateTime matchedAt;
    private LocalDateTime completedAt;
    
    private UUID recipientId;
    private UUID recipientUserId;
    private String availability;
    
    private String diagnosis;
    private String allergies;
    private String currentMedications;
    private String additionalNotes;
    
    private Boolean medicallyEligible;
    private Boolean legalClearance;
    private String notes;
    private LocalDate lastReviewed;
    
    private String hlaA1;
    private String hlaA2;
    private String hlaB1;
    private String hlaB2;
    private String hlaC1;
    private String hlaC2;
    private String hlaDR1;
    private String hlaDR2;
    private String hlaDQ1;
    private String hlaDQ2;
    private String hlaDP1;
    private String hlaDP2;
    private LocalDate testingDate;
    private String testingMethod;
    private String laboratoryName;
    private String certificationNumber;
    private String hlaString;
    private Boolean isHighResolution;
    
    private Boolean isConsented;
    private LocalDateTime consentedAt;
    
    private String locationData;
    
    private UUID receiveRequestId;
    private String requestType;
    private String requestedBloodType;
    private String requestedOrgan;
    private String requestedTissue;
    private String requestedStemCellType;
    private String urgencyLevel;
    private Double quantity;
    private LocalDate requestDate;
    private String requestStatus;
    private String requestNotes;
}