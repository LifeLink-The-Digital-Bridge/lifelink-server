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
    @JoinColumn(name = "donor_id", referencedColumnName = "id")
    private Donor donor;

    @Column(nullable = false)
    private Double hemoglobinLevel;

    @Column(nullable = false)
    private String bloodPressure;

    @Column(nullable = false)
    private Boolean hasDiseases;

    @Column(nullable = false)
    private Boolean takingMedication;

    @Column
    private String diseaseDescription;

    @Column
    private String currentMedications;

    @Column
    private LocalDate lastMedicalCheckup;

    @Column
    private String medicalHistory;

    @Column
    private Boolean hasInfectiousDiseases;

    @Column
    private String infectiousDiseaseDetails;

    @Column
    private Double creatinineLevel;

    @Column
    private String liverFunctionTests;

    @Column
    private String cardiacStatus;

    @Column
    private Double pulmonaryFunction;

    @Column
    private String overallHealthStatus;
}
