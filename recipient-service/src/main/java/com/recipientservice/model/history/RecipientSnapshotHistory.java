package com.recipientservice.model.history;

import com.recipientservice.enums.Availability;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "recipient_snapshot_history")
public class RecipientSnapshotHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "original_recipient_id")
    private UUID originalRecipientId;

    @Column(name = "user_id")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability")
    private Availability availability;

    @Column(name = "diagnosis")
    private String diagnosis;

    @Column(name = "allergies")
    private String allergies;

    @Column(name = "current_medications")
    private String currentMedications;

    @Column(name = "additional_notes")
    private String additionalNotes;

    @Column(name = "medically_eligible")
    private Boolean medicallyEligible;

    @Column(name = "legal_clearance")
    private Boolean legalClearance;

    @Column(name = "notes")
    private String notes;

    @Column(name = "last_reviewed")
    private LocalDate lastReviewed;
}
