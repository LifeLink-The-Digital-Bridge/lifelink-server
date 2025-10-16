package com.userservice.dto;

import com.userservice.enums.Visibility;
import lombok.Data;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

@Data
public class UserUpdateRequest {
    private String name;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    private String phone;

    private LocalDate dob;
    private String gender;
    private String profileImageUrl;
    private Visibility profileVisibility;
}
