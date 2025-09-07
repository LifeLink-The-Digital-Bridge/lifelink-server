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

    @Column(name = "medically_eligible")
    private Boolean medicallyEligible;

    @Column(name = "legal_clearance")
    private Boolean legalClearance;

    @Column(name = "notes")
    private String notes;

    @Column(name = "last_reviewed")
    private LocalDate lastReviewed;
}