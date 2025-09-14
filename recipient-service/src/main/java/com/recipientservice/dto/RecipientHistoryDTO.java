package com.recipientservice.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class RecipientHistoryDTO {
    private UUID matchId;
    private UUID donationId;
    private UUID donorUserId;
    private LocalDateTime matchedAt;
    private LocalDateTime completedAt;

    private RecipientSnapshotDTO recipientSnapshot;
    private MedicalDetailsDTO medicalDetailsSnapshot;
    private EligibilityCriteriaDTO eligibilityCriteriaSnapshot;
    private HLAProfileDTO hlaProfileSnapshot;
    private ReceiveRequestHistoryDTO receiveRequestSnapshot;
}
