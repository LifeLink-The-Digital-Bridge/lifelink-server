package com.healthservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthRecordCommentDTO {

    private UUID id;
    private UUID healthRecordId;
    private UUID userId;
    private String userRole;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
