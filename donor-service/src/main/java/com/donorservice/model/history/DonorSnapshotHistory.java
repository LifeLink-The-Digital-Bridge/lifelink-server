package com.donorservice.model.history;

import com.donorservice.enums.DonorStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "donor_snapshot_history")
public class DonorSnapshotHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "original_donor_id")
    private UUID originalDonorId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private DonorStatus status;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "age")
    private Integer age;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "medical_clearance")
    private Boolean medicalClearance;

    @Column(name = "recent_tattoo_or_piercing")
    private Boolean recentTattooOrPiercing;

    @Column(name = "recent_travel_details")
    private String recentTravelDetails;

    @Column(name = "recent_vaccination")
    private Boolean recentVaccination;

    @Column(name = "recent_surgery")
    private Boolean recentSurgery;

    @Column(name = "chronic_diseases")
    private String chronicDiseases;

    @Column(name = "allergies")
    private String allergies;

    @Column(name = "last_donation_date")
    private LocalDate lastDonationDate;

    @Column(name = "height")
    private Double height;

    @Column(name = "body_mass_index")
    private Double bodyMassIndex;

    @Column(name = "body_size")
    private String bodySize;

    @Column(name = "is_living_donor")
    private Boolean isLivingDonor;

    @Column(name = "hemoglobin_level")
    private Double hemoglobinLevel;

    @Column(name = "blood_pressure")
    private String bloodPressure;

    @Column(name = "has_diseases")
    private Boolean hasDiseases;

    @Column(name = "taking_medication")
    private Boolean takingMedication;

    @Column(name = "disease_description")
    private String diseaseDescription;

    @Column(name = "current_medications")
    private String currentMedications;

    @Column(name = "last_medical_checkup")
    private LocalDate lastMedicalCheckup;

    @Column(name = "medical_history")
    private String medicalHistory;

    @Column(name = "has_infectious_diseases")
    private Boolean hasInfectiousDiseases;

    @Column(name = "infectious_disease_details")
    private String infectiousDiseaseDetails;

    @Column(name = "creatinine_level")
    private Double creatinineLevel;

    @Column(name = "liver_function_tests")
    private String liverFunctionTests;

    @Column(name = "cardiac_status")
    private String cardiacStatus;

    @Column(name = "pulmonary_function")
    private Double pulmonaryFunction;

    @Column(name = "overall_health_status")
    private String overallHealthStatus;
}
