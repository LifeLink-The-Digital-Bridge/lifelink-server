package com.donorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateDonationHistoryRequest {
    private UUID matchId;
    private UUID receiveRequestId;
    private UUID recipientUserId;
    private LocalDateTime matchedAt;
    private LocalDateTime completedAt;

    private UUID donorId;
    private UUID donorUserId;
    private java.time.LocalDate registrationDate;
    private String donorStatus;

    private Double hemoglobinLevel;
    private String bloodPressure;
    private Boolean hasDiseases;
    private Boolean takingMedication;
    private String diseaseDescription;
    private String currentMedications;
    private java.time.LocalDate lastMedicalCheckup;
    private String medicalHistory;
    private Boolean hasInfectiousDiseases;
    private String infectiousDiseaseDetails;
    private Double creatinineLevel;
    private String liverFunctionTests;
    private String cardiacStatus;
    private Double pulmonaryFunction;
    private String overallHealthStatus;

    private Integer age;
    private java.time.LocalDate dob;
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
    private java.time.LocalDate testingDate;
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

    private UUID donationId;
    private java.time.LocalDate donationDate;
    private String donationStatus;
    private String bloodType;
    private String donationType;
    private Double quantity;

    private String organType;
    private Boolean isCompatible;
    private String organQuality;
    private LocalDateTime organViabilityExpiry;
    private Integer coldIschemiaTime;
    private Boolean organPerfused;
    private Double organWeight;
    private String organSize;
    private String functionalAssessment;
    private Boolean hasAbnormalities;
    private String abnormalityDescription;

    private String tissueType;
    private String stemCellType;
}