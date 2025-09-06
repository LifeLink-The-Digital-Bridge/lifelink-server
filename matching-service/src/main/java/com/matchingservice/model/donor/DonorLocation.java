package com.matchingservice.model.donor;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "donor_locations")
@Data
public class DonorLocation {

    @Id
    private UUID locationId;

    @Column(nullable = false)
    private UUID donorId;

    private String addressLine;
    private String landmark;
    private String area;
    private String city;
    private String district;
    private String state;
    private String country;
    private String pincode;

    private Double latitude;
    private Double longitude;
}
