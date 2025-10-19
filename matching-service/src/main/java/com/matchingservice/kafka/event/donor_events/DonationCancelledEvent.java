package com.matchingservice.kafka.event.donor_events;

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
public class DonationCancelledEvent {

    private UUID donationId;
    private UUID donorId;
    private UUID donorUserId;
    private String cancellationReason;
    private LocalDateTime cancelledAt;
    private String eventType;
}
