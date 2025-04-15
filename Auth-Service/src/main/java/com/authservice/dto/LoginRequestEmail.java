package com.authservice.dto;

import lombok.Data;

@Data
public class LoginRequestEmail {
    private String email;
    private String password;
}
