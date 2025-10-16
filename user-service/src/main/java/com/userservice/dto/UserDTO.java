package com.userservice.dto;

import com.userservice.enums.Visibility;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
@ToString
public class UserDTO {
    private UUID id;
    private String name;
    private String username;
    private String email;
    private String phone;
    private LocalDate dob;
    private String gender;
    private String profileImageUrl;
    private Set<String> roles;
    private Visibility profileVisibility;

}
