package com.donorservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "eligibility_criteria")
public class EligibilityCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "donor_id", referencedColumnName = "id")
    private Donor donor;

    @Column(nullable = false)
    private Boolean ageEligible;

    @Column
    private Integer age;

    @Column
    private LocalDate dob;

    @Column(nullable = false)
    private Boolean weightEligible;

    @Column
    private Double weight;

    @Column(nullable = false)
    private Boolean medicalClearance;

    @Column(nullable = false)
    private Boolean recentTattooOrPiercing;

    @Column
    private String recentTravelDetails;

    @Column
    private Boolean recentVaccination;

    @Column
    private Boolean recentSurgery;

    @Column
    private String chronicDiseases;

    @Column
    private String allergies;

    @Column
    private LocalDate lastDonationDate;
}

