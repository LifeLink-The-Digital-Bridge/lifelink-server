package com.donorservice.dto;

import com.donorservice.enums.DonationStatus;
import com.donorservice.enums.DonationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileLockInfoDTO {

    private boolean isLocked;
    private String reason;
    private List<ActiveDonationInfo> activeDonations;
    private List<String> lockedFields;
    private List<String> editableFields;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActiveDonationInfo {
        private UUID donationId;
        private DonationType donationType;
        private DonationStatus status;
        private LocalDate donationDate;
        private boolean canBeCancelled;
        private String cancelRestrictionReason;
    }
}
