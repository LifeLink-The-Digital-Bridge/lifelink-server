package com.donorservice.kafka.event;

import com.donorservice.enums.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DonationEvent {
    private UUID donationId;
    private UUID donorId;
    private UUID locationId;
    private DonationType donationType;
    private BloodType bloodType;
    private LocalDate donationDate;
    private DonationStatus status;
    private Double quantity;
    private OrganType organType;
    private Boolean isCompatible;
    private TissueType tissueType;
    private StemCellType stemCellType;

    private String organQuality;
    private LocalDateTime organViabilityExpiry;
    private Integer coldIschemiaTime;
    private Boolean organPerfused;
    private Double organWeight;
    private String organSize;
    private String functionalAssessment;
    private Boolean hasAbnormalities;
    private String abnormalityDescription;
}

