package com.healthservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "migrant_risk_scores", indexes = {
        @Index(name = "idx_migrant_risk_scores_user_id", columnList = "user_id")
})
public class MigrantRiskScore {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "health_id", nullable = false)
    private String healthId;

    @Column(name = "risk_score", nullable = false)
    private Double riskScore;

    @Column(name = "risk_level", nullable = false)
    private String riskLevel;

    @Column(name = "top_factors", columnDefinition = "text")
    private String topFactorsJson;

    @Column(name = "recommended_actions", columnDefinition = "text")
    private String recommendedActionsJson;

    @Column(name = "model_version")
    private String modelVersion;

    @Column(name = "computed_at")
    private LocalDateTime computedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
