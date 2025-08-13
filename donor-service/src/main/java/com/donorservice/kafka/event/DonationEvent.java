package com.donorservice.kafka.event;

import com.donorservice.enums.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class DonationEvent {
    private UUID donationId;
    private UUID donorId;
    private UUID locationId;
    private DonationType donationType;
    private String bloodType;
    private LocalDate donationDate;
    private String status;

    private Double quantity;
    private OrganType organType;
    private Boolean isCompatible;
    private TissueType tissueType;
    private StemCellType stemCellType;
}
