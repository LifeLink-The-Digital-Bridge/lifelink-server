package com.matchingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalDate;
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

    private UUID usedLocationId;
    private String usedAddressLine;
    private String usedLandmark;
    private String usedArea;
    private String usedCity;
    private String usedDistrict;
    private String usedState;
    private String usedCountry;
    private String usedPincode;
    private Double usedLatitude;
    private Double usedLongitude;

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
