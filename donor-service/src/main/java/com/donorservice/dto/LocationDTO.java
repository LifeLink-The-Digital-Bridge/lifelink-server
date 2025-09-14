package com.donorservice.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.util.UUID;

@Data
public class LocationDTO {
    private UUID id;

    @NotBlank(message = "Address line is required")
    @Size(max = 255, message = "Address line cannot exceed 255 characters")
    private String addressLine;

    @NotBlank(message = "Landmark is required")
    @Size(max = 100, message = "Landmark cannot exceed 100 characters")
    private String landmark;

    @NotBlank(message = "Area is required")
    @Size(max = 100, message = "Area cannot exceed 100 characters")
    private String area;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    @NotBlank(message = "District is required")
    @Size(max = 100, message = "District cannot exceed 100 characters")
    private String district;

    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State cannot exceed 100 characters")
    private String state;

    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country cannot exceed 100 characters")
    private String country;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "\\d{4,10}", message = "Pincode must be 4-10 digits")
    private String pincode;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double longitude;
}
