package com.matchingservice.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchFoundEvent {
    private UUID matchId;
    private UUID donationId;
    private UUID receiveRequestId;
    private UUID donorUserId;
    private UUID recipientUserId;
    private LocalDateTime matchedAt;
    private Double compatibilityScore;
    private Double distance;
}
