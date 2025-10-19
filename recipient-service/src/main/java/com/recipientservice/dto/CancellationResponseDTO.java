package com.recipientservice.dto;

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
public class CancellationResponseDTO {

    private boolean success;
    private String message;
    private UUID requestId;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private int expiredMatchesCount;
    private boolean profileUnlocked;
}
