package com.recipientservice.model.history;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "recipient_medical_details_snapshot_history")
public class RecipientMedicalDetailsSnapshotHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "diagnosis")
    private String diagnosis;

    @Column(name = "allergies")
    private String allergies;

    @Column(name = "current_medications")
    private String currentMedications;

    @Column(name = "additional_notes")
    private String additionalNotes;
}