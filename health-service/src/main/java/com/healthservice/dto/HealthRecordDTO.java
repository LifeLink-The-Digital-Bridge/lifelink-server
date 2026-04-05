package com.healthservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.healthservice.enums.RecordType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthRecordDTO {
    
    private UUID id;
    private UUID userId;
    private String healthId;
    private RecordType recordType;
    private String title;
    private String description;
    private String diagnosis;
    private String prescription;
    private String testResults;
    private String doctorName;
    private UUID doctorId;
    private String hospitalName;
    private String hospitalLocation;
    private LocalDate recordDate;
    private String documentUrl;
    @JsonProperty("isEmergency")
    @JsonAlias({"emergency"})
    private boolean isEmergency;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
