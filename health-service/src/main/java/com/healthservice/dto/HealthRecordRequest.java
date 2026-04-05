package com.healthservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.healthservice.enums.RecordType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthRecordRequest {
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    private String healthId;
    
    @NotNull(message = "Record type is required")
    private RecordType recordType;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    private String diagnosis;
    private String prescription;
    private String testResults;
    private String doctorName;
    private UUID doctorId;
    private String hospitalName;
    private String hospitalLocation;
    
    @NotNull(message = "Record date is required")
    private LocalDate recordDate;
    
    private String documentUrl;
    @JsonProperty("isEmergency")
    @JsonAlias({"emergency"})
    private boolean isEmergency;
    private String notes;
}
