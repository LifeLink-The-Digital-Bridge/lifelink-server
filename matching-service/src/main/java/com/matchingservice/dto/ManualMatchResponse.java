package com.matchingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManualMatchResponse {
    private boolean success;
    private String message;
    private UUID matchResultId;
    private MatchDetails matchDetails;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MatchDetails {
        private UUID donorId;
        private UUID recipientId;
        private UUID donationId;
        private UUID receiveRequestId;
        private String donationType;
        private String requestType;
        private String bloodType;
        private String matchType;
    }
}
