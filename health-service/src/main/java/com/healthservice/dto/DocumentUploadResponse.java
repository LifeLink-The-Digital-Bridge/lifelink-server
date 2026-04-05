package com.healthservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadResponse {
    private String documentUrl;
    private String fileName;
    private String contentType;
    private long size;
    private LocalDateTime uploadedAt;
}
