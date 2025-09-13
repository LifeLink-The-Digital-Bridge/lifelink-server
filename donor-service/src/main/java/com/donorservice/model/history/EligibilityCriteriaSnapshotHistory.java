package com.donorservice.model.history;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "eligibility_criteria_snapshot_history")
public class EligibilityCriteriaSnapshotHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "donor_id")
    private UUID donorId;

    @Column(name = "age")
    private Integer age;

    @Column(name = "age_eligible", nullable = false)
    private Boolean ageEligible;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "medical_clearance")
    private Boolean medicalClearance;

    @Column(name = "recent_tattoo_or_piercing")
    private Boolean recentTattooOrPiercing;

    @Column(name = "recent_travel_details")
    private String recentTravelDetails;

    @Column(name = "recent_vaccination")
    private Boolean recentVaccination;

    @Column(name = "recent_surgery")
    private Boolean recentSurgery;

    @Column(name = "chronic_diseases")
    private String chronicDiseases;

    @Column(name = "allergies")
    private String allergies;

    @Column(name = "last_donation_date")
    private LocalDate lastDonationDate;

    @Column(name = "height")
    private Double height;

    @Column(name = "body_mass_index")
    private Double bodyMassIndex;

    @Column(name = "body_size")
    private String bodySize;

    @Column(name = "is_living_donor")
    private Boolean isLivingDonor;
}
