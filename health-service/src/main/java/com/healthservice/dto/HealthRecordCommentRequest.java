package com.healthservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthRecordCommentRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotBlank(message = "User role is required")
    private String userRole;

    @NotBlank(message = "Comment is required")
    private String comment;
}
