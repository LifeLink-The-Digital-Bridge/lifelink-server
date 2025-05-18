package com.donorservice.dto;

import com.donorservice.enums.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class DonationRequestDTO {
    private UUID donorId;
    private DonationType donationType;
    private LocalDate donationDate;
    private Double quantity;
    private String status;
    private Long locationId;
    private BloodType bloodType;
    private OrganType organType;
    private Boolean isCompatible;
    private TissueType tissueType;
    private StemCellType stemCellType;
}
