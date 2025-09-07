package com.recipientservice.dto;

import com.recipientservice.enums.Availability;
import lombok.Data;
import java.util.UUID;

@Data
public class RecipientSnapshotDTO {
    private UUID originalRecipientId;
    private UUID userId;
    private Availability availability;
}