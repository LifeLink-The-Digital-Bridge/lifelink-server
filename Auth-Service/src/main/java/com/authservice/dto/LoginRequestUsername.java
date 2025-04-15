package com.authservice.dto;

import lombok.Data;

@Data
public class LoginRequestUsername {
    private String username;
    private String password;
}
