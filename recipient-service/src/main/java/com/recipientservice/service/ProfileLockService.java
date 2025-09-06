package com.recipientservice.service;

import com.recipientservice.enums.RequestStatus;
import com.recipientservice.repository.ReceiveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileLockService {
    
    private final ReceiveRequestRepository receiveRequestRepository;
    
    public boolean isRecipientProfileLocked(UUID recipientId) {
        return receiveRequestRepository.existsByRecipientIdAndStatus(recipientId, RequestStatus.PENDING);
    }
    
    public String getProfileLockReason(UUID recipientId) {
        if (isRecipientProfileLocked(recipientId)) {
            return "Profile is locked due to pending receive requests. Complete or cancel pending requests to update profile.";
        }
        return null;
    }
}