package com.donorservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class NearbyDonationActivityDTO {
    private PublicUserDTO user;
    private List<DonationDTO> pendingDonations;
}
