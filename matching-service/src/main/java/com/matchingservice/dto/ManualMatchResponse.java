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
public class ManualMatchResponse {
    private boolean success;
    private String message;
    private UUID matchResultId;
    private MatchDetails matchDetails;
    private ErrorDetails error;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MatchDetails {
        private UUID donationId;
        private UUID receiveRequestId;
        private UUID donorUserId;
        private UUID recipientUserId;
        private String donationType;
        private String requestType;
        private String bloodType;
        private String matchType;
        private LocalDateTime matchedAt;
        private String status;
        private Double distance;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ErrorDetails {
        private String errorType;
        private String errorMessage;
        private LocalDateTime timestamp;
    }
}
