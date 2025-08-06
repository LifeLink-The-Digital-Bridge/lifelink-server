package com.donorservice.kafka.event;

import com.donorservice.enums.DonationType;
import com.donorservice.enums.OrganType;
import com.donorservice.enums.StemCellType;
import com.donorservice.enums.TissueType;
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

    private Double quantity;
    private OrganType organType;
    private Boolean isCompatible;
    private TissueType tissueType;
    private StemCellType stemCellType;
}
