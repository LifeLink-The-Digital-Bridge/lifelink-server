package com.recipientservice.dto;

import com.recipientservice.enums.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class ReceiveRequestHistoryDTO {
    private UUID id;
    private UUID originalRequestId;
    private UUID recipientId;
    private UUID recipientUserId;

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
}
