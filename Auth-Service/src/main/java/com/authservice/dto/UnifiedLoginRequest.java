package com.authservice.dto;

import lombok.Data;

@Data
public class UnifiedLoginRequest {
    private String loginType;
    private String identifier;
    private String password;
}

