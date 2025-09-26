package com.matchingservice.model;

import com.matchingservice.enums.MatchStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "match_results")
@Data
public class MatchResult {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private UUID donationId;

    @Column(nullable = false)
    private UUID receiveRequestId;

    @Column(nullable = false)
    private UUID donorUserId;

    @Column(nullable = false)
    private UUID recipientUserId;

    @Column
    private UUID donorLocationId;

    @Column
    private UUID recipientLocationId;

    @Column
    private Double distance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status = MatchStatus.PENDING;

    @Column(nullable = false)
    private Boolean isConfirmed = false;

    @Column(nullable = false)
    private Boolean donorConfirmed = false;

    @Column(nullable = false)
    private Boolean recipientConfirmed = false;

    @Column
    private LocalDateTime donorConfirmedAt;

    @Column
    private LocalDateTime recipientConfirmedAt;

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

    @Column(name = "match_reason")
    private String matchReason;

    @Column(name = "priority_rank")
    private Integer priorityRank;

    @PrePersist
    public void setMatchedAt() {
        if (matchedAt == null) {
            matchedAt = LocalDateTime.now();
        }
    }
}
