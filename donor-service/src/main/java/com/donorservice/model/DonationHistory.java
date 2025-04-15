package com.donorservice.model;

import com.donorservice.enums.BloodType;
import com.donorservice.enums.DonationType;
import com.donorservice.enums.DonationStatus;
import com.donorservice.enums.OrganType;
import com.donorservice.enums.StemCellType;
import com.donorservice.enums.TissueType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "donation_history")
public class DonationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "donor_id", referencedColumnName = "id")
    private Donor donor;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID recipientId;

    @Column(nullable = false)
    private LocalDate donationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DonationType donationType;

    @Enumerated(EnumType.STRING)
    private BloodType bloodType;

    @Enumerated(EnumType.STRING)
    private OrganType organType;

    @Enumerated(EnumType.STRING)
    private TissueType tissueType;

    @Enumerated(EnumType.STRING)
    private StemCellType stemCellType;

    @Column(nullable = false)
    private Double quantity;

    @Column
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DonationStatus status;
}
