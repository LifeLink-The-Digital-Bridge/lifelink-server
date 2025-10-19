package com.matchingservice.model.donor;

import com.matchingservice.enums.BloodType;
import com.matchingservice.enums.DonationStatus;
import com.matchingservice.enums.DonationType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "donations")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
public class Donation {

    @Id
    private UUID donationId;

    @ManyToOne
    @JoinColumn(name = "donor_db_id", referencedColumnName = "id")
    private Donor donor;

    @Column(nullable = false)
    private UUID donorId;

    @Column(nullable = false)
    private UUID userId;

    @ManyToOne
    @JoinColumn(name = "location_db_id", referencedColumnName = "id")
    private DonorLocation location;

    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime eventTimestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "donation_type", nullable = false)
    private DonationType donationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_type")
    private BloodType bloodType;

    @Column(nullable = false)
    private LocalDate donationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DonationStatus status;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

}
