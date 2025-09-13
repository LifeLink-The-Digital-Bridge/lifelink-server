package com.donorservice.model.history;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "medical_details_snapshot_history")
public class MedicalDetailsSnapshotHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "donor_id")
    private UUID donorId;

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
