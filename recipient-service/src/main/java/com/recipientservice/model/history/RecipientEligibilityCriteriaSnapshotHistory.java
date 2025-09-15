package com.recipientservice.model.history;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "recipient_eligibility_criteria_snapshot_history")
public class RecipientEligibilityCriteriaSnapshotHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "age_eligible")
    private Boolean ageEligible;

    @Column(name = "age")
    private Integer age;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "weight_eligible")
    private Boolean weightEligible;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "height")
    private Double height;

    @Column(name = "body_mass_index")
    private Double bodyMassIndex;

    @Column(name = "body_size")
    private String bodySize;

    @Column(name = "is_living_donor")
    private Boolean isLivingDonor;

    @Column(name = "medically_eligible")
    private Boolean medicallyEligible;

    @Column(name = "legal_clearance")
    private Boolean legalClearance;

    @Column(name = "notes")
    private String notes;

    @Column(name = "last_reviewed")
    private LocalDate lastReviewed;
}
