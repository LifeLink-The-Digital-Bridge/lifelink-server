package com.healthservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class MigrantRiskScoreCallbackRequest {
    @NotNull
    private UUID userId;

    @NotBlank
    private String healthId;

    @NotNull
    private Double riskScore;

    @NotBlank
    private String riskLevel;

    @NotNull
    private List<String> topFactors;

    @NotNull
    private List<String> recommendedActions;

    private String modelVersion;

    private LocalDateTime computedAt;
}
