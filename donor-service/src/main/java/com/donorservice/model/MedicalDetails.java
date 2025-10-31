package com.donorservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "medical_details")
public class MedicalDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "donor_id", referencedColumnName = "id", nullable = false)
    private Donor donor;

    @Column(nullable = false)
    private Double hemoglobinLevel;

    @Column
    private Double bloodGlucoseLevel;

    @Column(nullable = false)
    private Boolean hasDiabetes = false;

    @Column(nullable = false)
    private String bloodPressure;

    @Column(nullable = false)
    private Boolean hasDiseases = false;

    @Column(nullable = false)
    private Boolean takingMedication = false;

    @Column
    private String diseaseDescription;

    @Column
    private String currentMedications;

    @Column(nullable = false)
    private LocalDate lastMedicalCheckup;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String medicalHistory;

    @Column(nullable = false)
    private Boolean hasInfectiousDiseases = false;

    @Column
    private String infectiousDiseaseDetails;

    @Column(nullable = false)
    private Double creatinineLevel;

    @Column(nullable = false)
    private String liverFunctionTests;

    @Column(nullable = false)
    private String cardiacStatus;

    @Column(nullable = false)
    private Double pulmonaryFunction;

    @Column(nullable = false)
    private String overallHealthStatus;
}
