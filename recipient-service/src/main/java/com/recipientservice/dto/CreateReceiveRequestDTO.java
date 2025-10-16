package com.recipientservice.dto;

import com.recipientservice.enums.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateReceiveRequestDTO {
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
    private String notes;
}

