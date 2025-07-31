package com.matchingservice.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "locations")
@Data
public class DonorLocation {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = false)
    private Donor donor;

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
