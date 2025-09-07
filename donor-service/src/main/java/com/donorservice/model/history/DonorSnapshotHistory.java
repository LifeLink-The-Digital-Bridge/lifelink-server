package com.donorservice.model.history;

import com.donorservice.enums.DonorStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "donor_snapshot_history")
public class DonorSnapshotHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "original_donor_id")
    private UUID originalDonorId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "registration_date")
    private LocalDate registrationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private DonorStatus status;
}