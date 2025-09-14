package com.donorservice.dto;

import com.donorservice.enums.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DonationHistoryDTO {
    private UUID id;
    private UUID donorId;
    private UUID locationId;

    private String usedLocationAddressLine;
    private String usedLocationLandmark;
    private String usedLocationArea;
    private String usedLocationCity;
    private String usedLocationDistrict;
    private String usedLocationState;
    private String usedLocationCountry;
    private String usedLocationPincode;
    private Double usedLocationLatitude;
    private Double usedLocationLongitude;

    private LocalDate donationDate;
    private DonationStatus status;
    private BloodType bloodType;
    private DonationType donationType;
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
