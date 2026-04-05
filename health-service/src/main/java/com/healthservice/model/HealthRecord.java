package com.healthservice.model;

import com.healthservice.enums.RecordType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "health_records", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_health_id", columnList = "health_id"),
    @Index(name = "idx_record_date", columnList = "record_date")
})
public class HealthRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "health_id")
    private String healthId;

    @Enumerated(EnumType.STRING)
    @Column(name = "record_type", nullable = false)
    private RecordType recordType;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "diagnosis", columnDefinition = "text")
    private String diagnosis;

    @Column(name = "prescription", columnDefinition = "text")
    private String prescription;

    @Column(name = "test_results", columnDefinition = "text")
    private String testResults;

    @Column(name = "doctor_name")
    private String doctorName;

    @Column(name = "doctor_id")
    private UUID doctorId;

    @Column(name = "hospital_name")
    private String hospitalName;

    @Column(name = "hospital_location")
    private String hospitalLocation;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "document_url", columnDefinition = "text")
    private String documentUrl;

    @Column(name = "is_emergency")
    private boolean isEmergency = false;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
