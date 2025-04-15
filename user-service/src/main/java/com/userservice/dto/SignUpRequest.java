package com.userservice.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SignUpRequest {
    private String name;
    private String email;
    private String username;
    private String password;
    private String phone;
    private LocalDate dob;
    private String gender;
    private String profileImageUrl;
}

