package com.recipientservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class NearbyRequestActivityDTO {
    private PublicUserDTO user;
    private List<ReceiveRequestDTO> pendingRequests;
}
