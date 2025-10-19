package com.recipientservice.dto;

import com.recipientservice.enums.RequestStatus;
import com.recipientservice.enums.RequestType;
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
    private List<ActiveRequestInfo> activeRequests;
    private List<String> lockedFields;
    private List<String> editableFields;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActiveRequestInfo {
        private UUID requestId;
        private RequestType requestType;
        private RequestStatus status;
        private LocalDate requestDate;
        private boolean canBeCancelled;
        private String cancelRestrictionReason;
    }
}
