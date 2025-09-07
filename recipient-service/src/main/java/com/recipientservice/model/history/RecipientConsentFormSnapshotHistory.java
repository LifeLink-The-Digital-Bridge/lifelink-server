package com.recipientservice.model.history;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "recipient_consent_form_snapshot_history")
public class RecipientConsentFormSnapshotHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "is_consented")
    private Boolean isConsented;

    @Column(name = "consented_at")
    private java.time.LocalDateTime consentedAt;
}