package com.donorservice.model.history;

import com.donorservice.enums.*;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "donation_snapshot_history")
public class DonationSnapshotHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "original_donation_id")
    private UUID originalDonationId;

    @Column(name = "donation_date")
    private LocalDate donationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private DonationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_type")
    private BloodType bloodType;

    @Enumerated(EnumType.STRING)
    @Column(name = "donation_type")
    private DonationType donationType;

    @Column(name = "quantity")
    private Double quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "organ_type")
    private OrganType organType;

    @Column(name = "is_compatible")
    private Boolean isCompatible;

    @Column(name = "organ_quality")
    private String organQuality;

    @Column(name = "organ_viability_expiry")
    private LocalDateTime organViabilityExpiry;

    @Column(name = "cold_ischemia_time")
    private Integer coldIschemiaTime;

    @Column(name = "organ_perfused")
    private Boolean organPerfused;

    @Column(name = "organ_weight")
    private Double organWeight;

    @Column(name = "organ_size")
    private String organSize;

    @Column(name = "functional_assessment")
    private String functionalAssessment;

    @Column(name = "has_abnormalities")
    private Boolean hasAbnormalities;

    @Column(name = "abnormality_description")
    private String abnormalityDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "tissue_type")
    private TissueType tissueType;

    @Enumerated(EnumType.STRING)
    @Column(name = "stem_cell_type")
    private StemCellType stemCellType;
}