package com.recipientservice.service;

import com.recipientservice.model.Recipient;
import org.springframework.http.ResponseEntity;

public interface RecipientService {

    ResponseEntity<?> addRecipient(Recipient recipient);

    ResponseEntity<?> getRecipient(Long id);
}
