package com.donorservice.model;

import jakarta.persistence.*;
import lombok.Data;

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
    private Double bloodPressure;

    @Column(nullable = false)
    private Boolean hasDiseases;

    @Column(nullable = false)
    private Boolean takingMedication;

    @Column
    private String diseaseDescription;
}
