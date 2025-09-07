package com.donorservice.dto;

import com.donorservice.enums.DonorStatus;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class DonorSnapshotDTO {
    private UUID originalDonorId;
    private UUID userId;
    private LocalDate registrationDate;
    private DonorStatus status;
}