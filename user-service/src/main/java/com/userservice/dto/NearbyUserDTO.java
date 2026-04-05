package com.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NearbyUserDTO {
    private UUID id;
    private String name;
    private String username;
    private String role;
    private Set<String> roles;
    private Double latitude;
    private Double longitude;
    private Double distanceKm;
    private String profileImageUrl;
    private String detail;
}
