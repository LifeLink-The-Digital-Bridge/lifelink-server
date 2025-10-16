package com.recipientservice.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
public class UserDTO {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private LocalDate dob;
    private String gender;
    private String profileImageUrl;
    private Set<String> roles;

}
