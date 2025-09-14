package com.matchingservice.dto;

import com.matchingservice.enums.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DonationDTO {
    private UUID id;
    private UUID donorId;
    private UUID locationId;
    private DonationType donationType;
    private LocalDate donationDate;
    private DonationStatus status;
    private BloodType bloodType;
    private Double quantity;

    private OrganType organType;
    private Boolean isCompatible;
    private String organQuality;
    private LocalDateTime organViabilityExpiry;
    private Integer coldIschemiaTime;
    private Boolean organPerfused;
    private Double organWeight;
    private String organSize;
    private String functionalAssessment;
    private Boolean hasAbnormalities;
    private String abnormalityDescription;

    private TissueType tissueType;
    private StemCellType stemCellType;
}