package com.matchingservice.dto;

import com.matchingservice.model.donor.MatchResult;
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
    private boolean isConfirmed;
    private LocalDateTime matchedAt;
    private Double distance;

    public static MatchResponse fromMatchResult(MatchResult matchResult) {
        return MatchResponse.builder()
                .matchResultId(matchResult.getId())
                .donationId(matchResult.getDonationId())
                .receiveRequestId(matchResult.getReceiveRequestId())
                .donorUserId(matchResult.getDonorUserId())
                .recipientUserId(matchResult.getRecipientUserId())
                .matchType("DONOR_TO_RECIPIENT")
                .isConfirmed(matchResult.getIsConfirmed())
                .matchedAt(matchResult.getMatchedAt())
                .distance(matchResult.getDistance())
                .build();
    }
}