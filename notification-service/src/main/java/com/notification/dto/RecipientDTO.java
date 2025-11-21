package com.notification.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class RecipientDTO {
    private UUID id;
    private UUID userId;
}
