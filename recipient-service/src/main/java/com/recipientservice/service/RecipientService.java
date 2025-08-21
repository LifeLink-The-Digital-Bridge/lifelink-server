package com.recipientservice.service;

import com.recipientservice.dto.CreateReceiveRequestDTO;
import com.recipientservice.dto.ReceiveRequestDTO;
import com.recipientservice.dto.RecipientDTO;
import com.recipientservice.dto.RegisterRecipientDTO;


import java.util.List;
import java.util.UUID;

public interface RecipientService {

    RecipientDTO createRecipient(UUID userId, RegisterRecipientDTO recipientDTO);

    ReceiveRequestDTO createReceiveRequest(UUID userId, CreateReceiveRequestDTO requestDTO);

    RecipientDTO getRecipientByUserId(UUID userId);

    RecipientDTO getRecipientById(UUID id);

    List<ReceiveRequestDTO> getReceiveRequestsByRecipientId(UUID recipientId);

}
