package com.matchingservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.matchingservice.enums.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReceiveRequestDTO {
    private UUID id;
    private UUID recipientId;
    private UUID locationId;
    private RequestType requestType;
    private BloodType requestedBloodType;
    private OrganType requestedOrgan;
    private TissueType requestedTissue;
    private StemCellType requestedStemCellType;
    private UrgencyLevel urgencyLevel;
    private Double quantity;
    private LocalDate requestDate;
    private RequestStatus status;
    private String notes;

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
}

