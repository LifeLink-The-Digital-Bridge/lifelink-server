package com.matchingservice.model;

import com.matchingservice.enums.BloodType;
import com.matchingservice.enums.DonationStatus;
import com.matchingservice.enums.DonationType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "donations")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID donationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = false)
    private Donor donor;

    @ManyToOne
    @JoinColumn(name = "location_id", referencedColumnName = "id")
    private DonorLocation location;

    @Enumerated(EnumType.STRING)
    private DonationType donationType;

    @Enumerated(EnumType.STRING)
    private BloodType bloodType;

    private LocalDate donationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DonationStatus status;

    @Embedded
    private LocationSummary locationSummary;
}
