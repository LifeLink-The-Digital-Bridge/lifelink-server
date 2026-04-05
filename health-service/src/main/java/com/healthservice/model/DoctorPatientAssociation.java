package com.healthservice.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "doctor_patient_associations",
       uniqueConstraints = @UniqueConstraint(columnNames = {"doctor_id", "patient_health_id"}))
public class DoctorPatientAssociation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID doctorId;

    @Column(nullable = false)
    private String patientHealthId;

    @Column(nullable = false)
    private UUID patientUserId;

    private LocalDateTime firstConsultationDate;

    private LocalDateTime lastConsultationDate;

    private Integer totalConsultations = 0;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private boolean isActive = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public DoctorPatientAssociation() {}

    public DoctorPatientAssociation(UUID doctorId, String patientHealthId, UUID patientUserId) {
        this.doctorId = doctorId;
        this.patientHealthId = patientHealthId;
        this.patientUserId = patientUserId;
        this.firstConsultationDate = LocalDateTime.now();
        this.lastConsultationDate = LocalDateTime.now();
        this.totalConsultations = 1;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(UUID doctorId) {
        this.doctorId = doctorId;
    }

    public String getPatientHealthId() {
        return patientHealthId;
    }

    public void setPatientHealthId(String patientHealthId) {
        this.patientHealthId = patientHealthId;
    }

    public UUID getPatientUserId() {
        return patientUserId;
    }

    public void setPatientUserId(UUID patientUserId) {
        this.patientUserId = patientUserId;
    }

    public LocalDateTime getFirstConsultationDate() {
        return firstConsultationDate;
    }

    public void setFirstConsultationDate(LocalDateTime firstConsultationDate) {
        this.firstConsultationDate = firstConsultationDate;
    }

    public LocalDateTime getLastConsultationDate() {
        return lastConsultationDate;
    }

    public void setLastConsultationDate(LocalDateTime lastConsultationDate) {
        this.lastConsultationDate = lastConsultationDate;
    }

    public Integer getTotalConsultations() {
        return totalConsultations;
    }

    public void setTotalConsultations(Integer totalConsultations) {
        this.totalConsultations = totalConsultations;
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
