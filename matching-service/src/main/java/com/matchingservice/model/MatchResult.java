package com.matchingservice.model;

import com.matchingservice.enums.MatchStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "match_results", indexes = {
        @Index(name = "idx_donor_user_matched_at", columnList = "donorUserId, matchedAt"),
        @Index(name = "idx_recipient_user_matched_at", columnList = "recipientUserId, matchedAt"),
        @Index(name = "idx_donation_compatibility", columnList = "donationId, compatibilityScore"),
        @Index(name = "idx_request_compatibility", columnList = "receiveRequestId, compatibilityScore"),
        @Index(name = "idx_status_matched_at", columnList = "status, matchedAt")
})

@Data
@EqualsAndHashCode(callSuper = false)
public class MatchResult {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    @Column(nullable = false)
    private UUID donationId;

    @NotNull
    @Column(nullable = false)
    private UUID receiveRequestId;

    @NotNull
    @Column(nullable = false)
    private UUID donorUserId;

    @NotNull
    @Column(nullable = false)
    private UUID recipientUserId;

    @Column
    private UUID donorLocationId;

    @Column
    private UUID recipientLocationId;

    @Column
    private Double distance;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status = MatchStatus.PENDING;

    @NotNull
    @Column(nullable = false)
    private Boolean isConfirmed = false;

    @NotNull
    @Column(nullable = false)
    private Boolean donorConfirmed = false;

    @NotNull
    @Column(nullable = false)
    private Boolean recipientConfirmed = false;

    @Column
    private LocalDateTime donorConfirmedAt;

    @Column
    private LocalDateTime recipientConfirmedAt;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime matchedAt;

    @Column(name = "compatibility_score")
    private Double compatibilityScore;

    @Column(name = "blood_compatibility_score")
    private Double bloodCompatibilityScore;

    @Column(name = "location_compatibility_score")
    private Double locationCompatibilityScore;

    @Column(name = "medical_compatibility_score")
    private Double medicalCompatibilityScore;

    @Column(name = "urgency_priority_score")
    private Double urgencyPriorityScore;

    @Column(name = "match_reason", length = 1000)
    private String matchReason;

    @Column(name = "priority_rank")
    private Integer priorityRank;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "expiry_reason", length = 500)
    private String expiryReason;

    @PrePersist
    public void setDefaults() {
        if (matchedAt == null) {
            matchedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = MatchStatus.PENDING;
        }
        if (isConfirmed == null) {
            isConfirmed = false;
        }
        if (donorConfirmed == null) {
            donorConfirmed = false;
        }
        if (recipientConfirmed == null) {
            recipientConfirmed = false;
        }
    }

    @PreUpdate
    public void updateExpiry() {
        if (status == MatchStatus.EXPIRED && expiredAt == null) {
            expiredAt = LocalDateTime.now();
        }
    }
}
