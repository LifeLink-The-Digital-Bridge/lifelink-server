package com.matchingservice.model.donor;

import com.matchingservice.enums.AlcoholStatus;
import com.matchingservice.enums.SmokingStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "donor_eligibility_criteria")
public class DonorEligibilityCriteria {

    @Id
    private Long eligibilityCriteriaId;

    @OneToOne
    @JoinColumn(name = "donor_db_id", referencedColumnName = "id")
    private Donor donor;

    @Column
    private Double weight;

    @Column
    private Integer age;

    @Column
    private LocalDate dob;

    @Column
    private Boolean medicalClearance;

    @Column
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

    @Column
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
