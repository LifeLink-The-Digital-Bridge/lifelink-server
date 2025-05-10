package com.authservice.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UnifiedLoginRequest {

    @NotBlank(message = "Login type is required")
    private String loginType;

    @NotBlank(message = "Identifier (username/email) is required")
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;
}


