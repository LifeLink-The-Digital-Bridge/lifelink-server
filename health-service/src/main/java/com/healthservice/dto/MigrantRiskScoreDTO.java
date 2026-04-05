package com.healthservice.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class MigrantRiskScoreDTO {
    private UUID id;
    private UUID userId;
    private String healthId;
    private Double riskScore;
    private String riskLevel;
    private List<String> topFactors;
    private List<String> recommendedActions;
    private String modelVersion;
    private LocalDateTime computedAt;
    private LocalDateTime updatedAt;
}
