package com.notification.dto;

import com.notification.enums.DonationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonationDTO {
    private UUID donationId;
    private UUID donorId;
    private UUID userId;
    private DonationType donationType;
    private String bloodType;
    private String organType;
    private String tissueType;
    private String stemCellType;
    private LocalDate donationDate;
    private String status;
}
