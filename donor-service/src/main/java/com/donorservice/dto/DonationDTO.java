package com.donorservice.dto;

import com.donorservice.enums.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DonationDTO {
    private Long id;
    private UUID donorId;
    private Long locationId;
    private DonationType donationType;
    private LocalDate donationDate;
    private String status;
    private BloodType bloodType;
    private Double quantity;
    private OrganType organType;
    private Boolean isCompatible;
    private TissueType tissueType;
    private StemCellType stemCellType;
}
