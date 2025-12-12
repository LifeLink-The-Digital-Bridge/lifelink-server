package com.recipientservice.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class PublicUserDTO {
    private UUID id;
    private String username;
    private String name;
    private String profileImageUrl;
}
