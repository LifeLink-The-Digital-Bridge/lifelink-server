package com.healthservice.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "connection_requests",
       indexes = {
           @Index(name = "idx_requester", columnList = "requester_user_id"),
           @Index(name = "idx_target", columnList = "target_user_id"),
           @Index(name = "idx_status", columnList = "status")
       })
public class ConnectionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID requesterUserId;

    @Column(nullable = false)
    private String requesterRole;

    @Column(nullable = false)
    private UUID targetUserId;

    @Column(nullable = false)
    private String targetRole;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String requestType;

    @Column(columnDefinition = "TEXT")
    private String message;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime respondedAt;

    public ConnectionRequest() {}

    public ConnectionRequest(UUID requesterUserId, String requesterRole, UUID targetUserId, 
                           String targetRole, String requestType, String message) {
        this.requesterUserId = requesterUserId;
        this.requesterRole = requesterRole;
        this.targetUserId = targetUserId;
        this.targetRole = targetRole;
        this.requestType = requestType;
        this.message = message;
        this.status = "PENDING";
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getRequesterUserId() {
        return requesterUserId;
    }

    public void setRequesterUserId(UUID requesterUserId) {
        this.requesterUserId = requesterUserId;
    }

    public String getRequesterRole() {
        return requesterRole;
    }

    public void setRequesterRole(String requesterRole) {
        this.requesterRole = requesterRole;
    }

    public UUID getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(UUID targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }
}
