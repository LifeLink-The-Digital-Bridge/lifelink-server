// MLMatchResult.java
package com.matchingservice.dto.ml;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MLMatchResult {

    private UUID donationId;
    private UUID receiveRequestId;
    private UUID donorUserId;
    private UUID recipientUserId;
    private UUID donorLocationId;
    private UUID recipientLocationId;

    private Double compatibilityScore;
    private Double bloodCompatibilityScore;
    private Double locationCompatibilityScore;
    private Double medicalCompatibilityScore;
    private Double hlaCompatibilityScore;
    private Double urgencyPriorityScore;

    private Double distanceKm;
    private String matchReason;
    private Integer priorityRank;
    private Integer hlaMismatchCount;

    private Double mlConfidence;
}
