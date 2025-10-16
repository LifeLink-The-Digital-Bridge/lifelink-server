package com.matchingservice.model.recipients;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "recipient_medical_details")
public class RecipientMedicalDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private Long medicalDetailsId;

    @OneToOne
    @JoinColumn(name = "recipient_db_id", referencedColumnName = "id")
    private Recipient recipient;

    @Column(nullable = false)
    private Double hemoglobinLevel;

    @Column
    private Double bloodGlucoseLevel;

    @Column
    private Boolean hasDiabetes;

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
