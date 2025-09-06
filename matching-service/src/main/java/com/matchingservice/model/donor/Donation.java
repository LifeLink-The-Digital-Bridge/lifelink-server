package com.matchingservice.model.donor;

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
    private UUID donationId;

    @Column(nullable = false)
    private UUID donorId;

    @Column(nullable = false)
    private UUID userId;

    @Column
    private UUID locationId;

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
