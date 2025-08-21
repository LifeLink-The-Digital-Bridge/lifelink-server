package com.recipientservice.model;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "recipient_medical_details")
public class MedicalDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "recipient_id", referencedColumnName = "id", unique = true)
    private Recipient recipient;

    @Column(nullable = false)
    private Double hemoglobinLevel;

    @Column(nullable = false)
    private String bloodPressure;

    @Column
    private String diagnosis;

    @Column
    private String allergies;

    @Column
    private String currentMedications;

    @Column
    private String additionalNotes;

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

