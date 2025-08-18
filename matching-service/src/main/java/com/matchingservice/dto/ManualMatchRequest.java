package com.matchingservice.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ManualMatchRequest {
    private UUID donationId;
    private UUID receiveRequestId;
    private UUID donorLocationId;
    private UUID recipientLocationId;
}
