package com.healthservice.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ngo_migrant_associations",
       uniqueConstraints = @UniqueConstraint(columnNames = {"ngo_id", "migrant_health_id"}))
public class NGOMigrantAssociation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID ngoId;

    @Column(nullable = false)
    private String migrantHealthId;

    @Column(nullable = false)
    private UUID migrantUserId;

    @Column(nullable = false)
    private String supportType;

    @Column(nullable = false)
    private String status;

    private LocalDateTime supportStartDate;

    private LocalDateTime supportEndDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private boolean isActive = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public NGOMigrantAssociation() {}

    public NGOMigrantAssociation(UUID ngoId, String migrantHealthId, UUID migrantUserId, 
                                 String supportType, String status) {
        this.ngoId = ngoId;
        this.migrantHealthId = migrantHealthId;
        this.migrantUserId = migrantUserId;
        this.supportType = supportType;
        this.status = status;
        this.supportStartDate = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getNgoId() {
        return ngoId;
    }

    public void setNgoId(UUID ngoId) {
        this.ngoId = ngoId;
    }

    public String getMigrantHealthId() {
        return migrantHealthId;
    }

    public void setMigrantHealthId(String migrantHealthId) {
        this.migrantHealthId = migrantHealthId;
    }

    public UUID getMigrantUserId() {
        return migrantUserId;
    }

    public void setMigrantUserId(UUID migrantUserId) {
        this.migrantUserId = migrantUserId;
    }

    public String getSupportType() {
        return supportType;
    }

    public void setSupportType(String supportType) {
        this.supportType = supportType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getSupportStartDate() {
        return supportStartDate;
    }

    public void setSupportStartDate(LocalDateTime supportStartDate) {
        this.supportStartDate = supportStartDate;
    }

    public LocalDateTime getSupportEndDate() {
        return supportEndDate;
    }

    public void setSupportEndDate(LocalDateTime supportEndDate) {
        this.supportEndDate = supportEndDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
