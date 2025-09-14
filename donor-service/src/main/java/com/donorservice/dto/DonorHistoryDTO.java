package com.donorservice.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DonorHistoryDTO {
    private UUID matchId;
    private UUID receiveRequestId;
    private UUID recipientUserId;
    private LocalDateTime matchedAt;
    private LocalDateTime completedAt;

    private DonorSnapshotDTO donorSnapshot;
    private MedicalDetailsDTO medicalDetailsSnapshot;
    private EligibilityCriteriaDTO eligibilityCriteriaSnapshot;
    private HLAProfileDTO hlaProfileSnapshot;
    private DonationHistoryDTO donationSnapshot;
}