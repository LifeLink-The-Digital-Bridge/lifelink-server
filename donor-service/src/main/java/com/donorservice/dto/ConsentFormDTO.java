package com.donorservice.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ConsentFormDTO {
    private Long id;
    private UUID userId;
    private UUID donorId;
    private Boolean isConsented;
    private LocalDateTime consentedAt;
    private String consentType;
}

