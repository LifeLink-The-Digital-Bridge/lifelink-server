package com.donorservice.model;

import com.donorservice.enums.SmokingStatus;
import com.donorservice.enums.AlcoholStatus;
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
    @JoinColumn(name = "donor_id", referencedColumnName = "id", nullable = false)
    private Donor donor;

    @Column(nullable = false)
    private Boolean ageEligible;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false)
    private LocalDate dob;

    @Column(nullable = false)
    private Boolean weightEligible;

    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false)
    private Boolean medicalClearance = false;

    @Column(nullable = false)
    private Boolean recentTattooOrPiercing = false;

    @Column(columnDefinition = "TEXT")
    private String recentTravelDetails;

    @Column(nullable = false)
    private Boolean recentVaccination = false;

    @Column(nullable = false)
    private Boolean recentSurgery = false;

    @Column(columnDefinition = "TEXT")
    private String chronicDiseases;

    @Column(columnDefinition = "TEXT")
    private String allergies;

    @Column
    private LocalDate lastDonationDate;

    @Column(nullable = false)
    private Double height;

    @Column(nullable = false)
    private Double bodyMassIndex;

    @Column
    private String bodySize;

    @Column(nullable = false)
    private Boolean isLivingDonor = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "smoking_status")
    private SmokingStatus smokingStatus;

    @Column(name = "pack_years")
    private Integer packYears;

    @Column(name = "quit_smoking_date")
    private LocalDate quitSmokingDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "alcohol_status")
    private AlcoholStatus alcoholStatus;

    @Column(name = "drinks_per_week")
    private Integer drinksPerWeek;

    @Column(name = "quit_alcohol_date")
    private LocalDate quitAlcoholDate;

    @Column(name = "alcohol_abstinence_months")
    private Integer alcoholAbstinenceMonths;
}

