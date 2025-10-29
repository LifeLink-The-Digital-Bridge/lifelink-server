package com.matchingservice.dto.ml;

import com.matchingservice.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MLDonationData {

    private UUID donationId;
    private UUID donorId;
    private UUID userId;
    private UUID locationId;

    private DonationType donationType;
    private BloodType bloodType;
    private LocalDate donationDate;
    private Double quantity;

    private OrganType organType;
    private String organQuality;
    private LocalDateTime organViabilityExpiry;
    private Double organViabilityHours;
    private Integer coldIschemiaTime;
    private Boolean organPerfused;
    private Double organWeight;
    private String organSize;
    private Boolean hasAbnormalities;

    private TissueType tissueType;
    private StemCellType stemCellType;

    private Integer age;
    private LocalDate dob;
    private Double weight;
    private Double height;
    private Double bmi;
    private String bodySize;
    private Boolean isLivingDonor;

    private Double hemoglobinLevel;
    private Double bloodGlucoseLevel;
    private Boolean hasDiabetes;
    private String bloodPressure;
    private Boolean hasDiseases;
    private Boolean hasInfectiousDiseases;
    private String infectiousDiseaseDetails;
    private Double creatinineLevel;
    private String liverFunctionTests;
    private String cardiacStatus;
    private Double pulmonaryFunction;
    private String overallHealthStatus;

    private Boolean medicalClearance;
    private Boolean recentTattoo;
    private Boolean recentVaccination;
    private Boolean recentSurgery;
    private String chronicDiseases;
    private String allergies;
    private LocalDate lastDonationDate;
    private Integer daysSinceLastDonation;

    private SmokingStatus smokingStatus;
    private Integer packYears;
    private LocalDate quitSmokingDate;
    private AlcoholStatus alcoholStatus;
    private Integer drinksPerWeek;
    private LocalDate quitAlcoholDate;
    private Integer alcoholAbstinenceMonths;

    private Double latitude;
    private Double longitude;
    private String city;
    private String district;
    private String state;
    private String country;

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
    private Boolean hlaHighResolution;
    private String hlaString;
}
