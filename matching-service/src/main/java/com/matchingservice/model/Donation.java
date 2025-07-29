package com.matchingservice.model;

import com.matchingservice.enums.DonationType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "donations")
@Data
public class Donation {

    @Id
    private UUID donationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = false)
    private Donor donor;

    @ManyToOne
    @JoinColumn(name = "location_id", referencedColumnName = "id")
    private Location location;

    @Enumerated(EnumType.STRING)
    private DonationType donationType;

    private String bloodType;
    private LocalDate donationDate;

    @Embedded
    private LocationSummary locationSummary;
}

