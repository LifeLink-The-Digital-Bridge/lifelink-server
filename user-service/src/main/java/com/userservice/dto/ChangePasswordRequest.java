package com.userservice.dto;

import lombok.Data;

@Data
public class ChangePasswordRequest {
    private String email;
    private String newPassword;
    private String repeatPassword;

    public ChangePasswordRequest(String email, String newPassword, String repeatPassword) {
        this.email = email;
        this.newPassword = newPassword;
        this.repeatPassword = repeatPassword;
    }

}

