package com.donorservice.service;

import com.donorservice.dto.DonationDTO;
import com.donorservice.dto.DonationRequestDTO;
import com.donorservice.dto.DonorDTO;
import com.donorservice.dto.RegisterDonor;
import com.donorservice.enums.DonationStatus;

import java.util.List;
import java.util.UUID;

public interface DonorService {
    DonorDTO saveOrUpdateDonor(UUID userId, RegisterDonor donorDTO);

    DonorDTO getDonorById(UUID id);

    DonationDTO registerDonation(DonationRequestDTO donationDTO);

    List<DonationDTO> getDonationsByDonorId(UUID donorId);

    DonorDTO getDonorByUserId(UUID userId);
    
    List<DonationDTO> getDonationsByUserId(UUID userId);
    
    void updateDonationStatus(UUID donationId, DonationStatus status);
}
