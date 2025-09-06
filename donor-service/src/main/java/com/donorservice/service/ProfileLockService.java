package com.donorservice.service;

import com.donorservice.enums.DonationStatus;
import com.donorservice.repository.DonationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileLockService {
    
    private final DonationRepository donationRepository;
    
    public boolean isDonorProfileLocked(UUID donorId) {
        return donationRepository.existsByDonorIdAndStatus(donorId, DonationStatus.PENDING);
    }
    
    public String getProfileLockReason(UUID donorId) {
        if (isDonorProfileLocked(donorId)) {
            return "Profile is locked due to pending donations. Complete or cancel pending donations to update profile.";
        }
        return null;
    }
}