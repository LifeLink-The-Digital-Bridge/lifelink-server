package com.donorservice.service;

import com.donorservice.dto.DonationDTO;
import com.donorservice.dto.DonationRequestDTO;
import com.donorservice.dto.DonorDTO;
import com.donorservice.dto.RegisterDonor;

import java.util.List;
import java.util.UUID;

public interface DonorService {
    DonorDTO createDonor(UUID userId, RegisterDonor donorDTO);
    DonorDTO getDonorById(UUID id);

    DonationDTO registerDonation(DonationRequestDTO donationDTO);

    List<DonationDTO> getDonationsByDonorId(UUID donorId);
}
