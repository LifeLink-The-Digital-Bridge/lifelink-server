package com.healthservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "migrant_profiles")
public class MigrantProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "health_id")
    private String healthId;

    @Column(name = "blood_group")
    private String bloodGroup;

    @Column(name = "height_cm")
    private Double heightCm;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Column(name = "allergies", columnDefinition = "text")
    private String allergies;

    @Column(name = "chronic_conditions", columnDefinition = "text")
    private String chronicConditions;

    @Column(name = "current_medications", columnDefinition = "text")
    private String currentMedications;

    @Column(name = "vaccination_status", columnDefinition = "text")
    private String vaccinationStatus;

    @Column(name = "health_risk_score")
    private String healthRiskScore;

    @Column(name = "last_checkup_date")
    private LocalDateTime lastCheckupDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
