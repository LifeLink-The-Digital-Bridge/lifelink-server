package com.donorservice.service;

import com.donorservice.dto.DonorDTO;
import com.donorservice.dto.RegisterDonor;

import java.util.UUID;

public interface DonorService {
    DonorDTO createDonor(RegisterDonor donorDTO);
    DonorDTO getDonorById(UUID id);
}
