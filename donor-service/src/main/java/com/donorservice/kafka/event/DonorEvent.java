package com.donorservice.kafka.event;

import com.donorservice.enums.AlcoholStatus;
import com.donorservice.enums.SmokingStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class DonorEvent {
    private UUID donorId;
    private UUID userId;
    private LocalDate registrationDate;
    private String status;

    private Long eligibilityCriteriaId;
    private Double weight;
    private Integer age;
    private LocalDate dob;
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

    private SmokingStatus smokingStatus;
    private Integer packYears;
    private LocalDate quitSmokingDate;

    private AlcoholStatus alcoholStatus;
    private Integer drinksPerWeek;
    private LocalDate quitAlcoholDate;
    private Integer alcoholAbstinenceMonths;


    private Long medicalDetailsId;
    private Double hemoglobinLevel;
    private Double bloodGlucoseLevel;
    private Boolean hasDiabetes;
    private String bloodPressure;
    private Boolean hasDiseases;
    private Boolean takingMedication;
    private String diseaseDescription;

    private String currentMedications;
    private LocalDate lastMedicalCheckup;
    private String medicalHistory;
    private Boolean hasInfectiousDiseases;
    private String infectiousDiseaseDetails;
    private Double creatinineLevel;
    private String liverFunctionTests;
    private String cardiacStatus;
    private Double pulmonaryFunction;
    private String overallHealthStatus;
}
