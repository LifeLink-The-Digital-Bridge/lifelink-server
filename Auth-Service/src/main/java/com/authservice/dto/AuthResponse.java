package com.authservice.dto;

import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private UUID id;
    private String email;
    private String username;
    private Set<String> roles;

    public AuthResponse(String accessToken, String refreshToken, UUID id, String email, String username, Set<String> roles) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.id = id;
        this.email = email;
        this.username = username;
        this.roles = roles;
    }

}



