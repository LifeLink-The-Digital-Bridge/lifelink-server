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

    @Column
    private Double height;

    @Column
    private Double bodyMassIndex;

    @Column
    private String bodySize;

    @Column(nullable = false)
    private Boolean isLivingDonor;

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
