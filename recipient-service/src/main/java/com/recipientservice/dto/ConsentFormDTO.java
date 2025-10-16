package com.recipientservice.dto;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ConsentFormDTO {
    private Long id;
    private UUID userId;
    private Boolean isConsented;
    private LocalDateTime consentedAt;
}
