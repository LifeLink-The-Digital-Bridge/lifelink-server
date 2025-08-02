package com.matchingservice.model.recipients;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "recipient_locations")
@Data
public class RecipientLocation {

    @Id
    private Long id;

    @Column(nullable = false)
    private UUID recipientId;

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
