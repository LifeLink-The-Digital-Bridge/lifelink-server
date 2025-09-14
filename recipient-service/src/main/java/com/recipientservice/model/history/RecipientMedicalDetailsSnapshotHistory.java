package com.recipientservice.model.history;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "recipient_medical_details_snapshot_history")
public class RecipientMedicalDetailsSnapshotHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // Vitals/conditions mirrored from matching DTO
    @Column(name = "hemoglobin_level")
    private Double hemoglobinLevel;

    @Column(name = "blood_pressure")
    private String bloodPressure;

    @Column(name = "diagnosis")
    private String diagnosis;

    @Column(name = "allergies")
    private String allergies;

    @Column(name = "current_medications")
    private String currentMedications;

    @Column(name = "additional_notes")
    private String additionalNotes;

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
