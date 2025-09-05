package com.matchingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchDetailsResponse {
    private UUID matchId;
    private boolean isConfirmed;
    private LocalDateTime matchedAt;
    private Double distance;

    private UUID donorId;
    private UUID recipientId;

    private UUID donationId;
    private UUID receiveRequestId;

    private String matchSummary;
    private String compatibilityScore;
    private String recommendedActions;
}
