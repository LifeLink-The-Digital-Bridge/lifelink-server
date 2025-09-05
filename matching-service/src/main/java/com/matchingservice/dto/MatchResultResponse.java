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
public class MatchResultResponse {
    private UUID matchId;
    private boolean isConfirmed;
    private LocalDateTime matchedAt;
    private Double distance;
    
    private UUID donationId;
    private UUID receiveRequestId;
    private UUID donorUserId;
    private UUID recipientId;

    public static MatchResultResponse fromMatchResult(MatchResult matchResult) {
        return MatchResultResponse.builder()
                .matchId(matchResult.getId())
                .isConfirmed(matchResult.getIsConfirmed())
                .matchedAt(matchResult.getMatchedAt())
                .distance(matchResult.getDistance())
                .donationId(matchResult.getDonation() != null ? matchResult.getDonation().getDonationId() : null)
                .receiveRequestId(matchResult.getReceiveRequest() != null ? matchResult.getReceiveRequest().getReceiveRequestId() : null)
                .donorUserId(matchResult.getDonation() != null && matchResult.getDonation().getDonor() != null ? 
                    matchResult.getDonation().getDonor().getUserId() : null)
                .recipientId(matchResult.getReceiveRequest() != null ? matchResult.getReceiveRequest().getRecipientId() : null)
                .build();
    }
}
