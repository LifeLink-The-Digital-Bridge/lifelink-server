package com.recipientservice.service;

import com.recipientservice.enums.*;
import com.recipientservice.model.Recipient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class RecipientServiceImp implements RecipientService {
    public ResponseEntity<?> addRecipient(Recipient recipient) {
        // Logic to save recipient data.
        return ResponseEntity.ok(recipient);
    }

    public ResponseEntity<?> getRecipient(Long id) {
        // Dummy recipient for demonstration.
        Recipient recipient = new Recipient();
        recipient.setId(id);
        recipient.setUserId(2L);
        recipient.setAvailability(Availability.AVAILABLE);
        recipient.setRequiredBloodType(BloodType.A_PLUS);
        recipient.setOrganNeeded("Kidney");
        recipient.setUrgencyLevel(UrgencyLevel.HIGH);
        return ResponseEntity.ok(recipient);
    }
}
