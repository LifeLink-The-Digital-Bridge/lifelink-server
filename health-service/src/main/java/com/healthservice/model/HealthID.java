package com.healthservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "health_ids")
public class HealthID {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "health_id", unique = true, nullable = false)
    private String healthId;

    @Column(name = "qr_code_data", columnDefinition = "text")
    private String qrCodeData;

    @Column(name = "emergency_pin")
    private String emergencyPin;

    @Column(name = "blood_group")
    private String bloodGroup;

    @Column(name = "allergies", columnDefinition = "text")
    private String allergies;

    @Column(name = "chronic_conditions", columnDefinition = "text")
    private String chronicConditions;

    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone")
    private String emergencyContactPhone;

    @Column(name = "rh_factor")
    private String rhFactor;

    @Column(name = "height_cm")
    private Double heightCm;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Column(name = "current_medications", columnDefinition = "TEXT")
    private String currentMedications;

    @Column(name = "vaccination_status", columnDefinition = "TEXT")
    private String vaccinationStatus;

    @Column(name = "medical_history", columnDefinition = "TEXT")
    private String medicalHistory;

    @Column(name = "has_chronic_diseases")
    private Boolean hasChronicDiseases = false;

    @Column(name = "has_diabetes")
    private Boolean hasDiabetes = false;

    @Column(name = "blood_pressure")
    private String bloodPressure;

    @Column(name = "hemoglobin_level")
    private Double hemoglobinLevel;

    @Column(name = "last_checkup_date")
    private LocalDateTime lastCheckupDate;

    @Column(name = "current_city")
    private String currentCity;

    @Column(name = "current_state")
    private String currentState;

    @Column(name = "occupation")
    private String occupation;

    @Column(name = "preferred_language")
    private String preferredLanguage;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
