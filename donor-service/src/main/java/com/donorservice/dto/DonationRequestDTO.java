package com.donorservice.dto;

import com.donorservice.enums.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DonationRequestDTO {
    private UUID donorId;
    private DonationType donationType;
    private LocalDate donationDate;
    private UUID locationId;
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