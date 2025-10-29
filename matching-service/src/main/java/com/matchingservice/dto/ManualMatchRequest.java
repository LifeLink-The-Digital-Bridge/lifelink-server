package com.matchingservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class ManualMatchRequest {
    @NotNull(message = "Donation ID is required")
    private UUID donationId;

    @NotNull(message = "Receive Request ID is required")
    private UUID receiveRequestId;
}
