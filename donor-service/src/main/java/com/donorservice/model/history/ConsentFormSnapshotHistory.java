package com.donorservice.model.history;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "consent_form_snapshot_history")
public class ConsentFormSnapshotHistory {

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