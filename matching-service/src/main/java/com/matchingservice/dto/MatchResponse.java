package com.matchingservice.dto;

import com.matchingservice.enums.MatchStatus;
import com.matchingservice.model.MatchResult;
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
public class MatchResponse {
    private UUID matchResultId;
    private UUID donationId;
    private UUID receiveRequestId;
    private UUID donorUserId;
    private UUID recipientUserId;
    private String donationType;
    private String requestType;
    private String bloodType;
    private String matchType;
    private MatchStatus status;
    private Boolean isConfirmed;
    private Boolean donorConfirmed;
    private Boolean recipientConfirmed;
    private LocalDateTime donorConfirmedAt;
    private LocalDateTime recipientConfirmedAt;
    private LocalDateTime matchedAt;
    private LocalDateTime expiredAt;
    private String expiryReason;
    private Double distance;
    private Double compatibilityScore;
    private Double bloodCompatibilityScore;
    private Double locationCompatibilityScore;
    private Double medicalCompatibilityScore;
    private Double urgencyPriorityScore;
    private String matchReason;
    private Integer priorityRank;

    public static MatchResponse fromMatchResult(MatchResult matchResult) {
        return MatchResponse.builder()
                .matchResultId(matchResult.getId())
                .donationId(matchResult.getDonationId())
                .receiveRequestId(matchResult.getReceiveRequestId())
                .donorUserId(matchResult.getDonorUserId())
                .recipientUserId(matchResult.getRecipientUserId())
                .status(matchResult.getStatus())
                .matchType("DONOR_TO_RECIPIENT")
                .isConfirmed(matchResult.getIsConfirmed())
                .donorConfirmed(matchResult.getDonorConfirmed())
                .recipientConfirmed(matchResult.getRecipientConfirmed())
                .donorConfirmedAt(matchResult.getDonorConfirmedAt())
                .recipientConfirmedAt(matchResult.getRecipientConfirmedAt())
                .matchedAt(matchResult.getMatchedAt())
                .expiredAt(matchResult.getExpiredAt())
                .expiryReason(matchResult.getExpiryReason())
                .distance(matchResult.getDistance())
                .compatibilityScore(matchResult.getCompatibilityScore())
                .bloodCompatibilityScore(matchResult.getBloodCompatibilityScore())
                .locationCompatibilityScore(matchResult.getLocationCompatibilityScore())
                .medicalCompatibilityScore(matchResult.getMedicalCompatibilityScore())
                .urgencyPriorityScore(matchResult.getUrgencyPriorityScore())
                .matchReason(matchResult.getMatchReason())
                .priorityRank(matchResult.getPriorityRank())
                .build();
    }
}
