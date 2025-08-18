package com.matchingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManualMatchResponse {
    private boolean success;
    private String message;
    private UUID matchResultId;
}
