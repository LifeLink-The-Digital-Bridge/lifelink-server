package com.recipientservice.kafka.events;

import com.recipientservice.enums.*;
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
public class RequestCancelledEvent {

    private UUID requestId;
    private UUID recipientId;
    private UUID recipientUserId;
    private String cancellationReason;
    private LocalDateTime cancelledAt;
    private String eventType;
    
    private RequestType requestType;
    private BloodType requestedBloodType;
    private OrganType requestedOrgan;
    private TissueType requestedTissue;
    private StemCellType requestedStemCellType;
}
