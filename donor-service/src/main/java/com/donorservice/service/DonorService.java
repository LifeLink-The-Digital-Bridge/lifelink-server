package com.donorservice.service;

import com.donorservice.dto.*;
import com.donorservice.enums.DonationStatus;

import java.util.List;
import java.util.UUID;

public interface DonorService {
    DonorDTO saveOrUpdateDonor(UUID userId, RegisterDonor donorDTO);

    DonorDTO getDonorById(UUID id);

    DonationDTO registerDonation(DonationRequestDTO donationDTO);

    DonorDTO getDonorByUserId(UUID userId);
    
    List<DonationDTO> getDonationsByUserId(UUID userId);
    
    void updateDonationStatus(UUID donationId, DonationStatus status);
    
    String getDonationStatus(UUID donationId);
    
    DonationDTO getDonationById(UUID donationId);

    List<DonationDTO> getDonationsByDonorId(UUID donorId, UUID requesterId);
}
