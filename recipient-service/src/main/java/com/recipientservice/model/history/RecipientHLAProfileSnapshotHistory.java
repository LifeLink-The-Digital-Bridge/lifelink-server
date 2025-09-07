package com.recipientservice.model.history;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "recipient_hla_profile_snapshot_history")
public class RecipientHLAProfileSnapshotHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "hla_a1")
    private String hlaA1;

    @Column(name = "hla_a2")
    private String hlaA2;

    @Column(name = "hla_b1")
    private String hlaB1;

    @Column(name = "hla_b2")
    private String hlaB2;

    @Column(name = "hla_c1")
    private String hlaC1;

    @Column(name = "hla_c2")
    private String hlaC2;

    @Column(name = "hla_dr1")
    private String hlaDr1;

    @Column(name = "hla_dr2")
    private String hlaDr2;

    @Column(name = "hla_dq1")
    private String hlaDq1;

    @Column(name = "hla_dq2")
    private String hlaDq2;

    @Column(name = "hla_dp1")
    private String hlaDP1;

    @Column(name = "hla_dp2")
    private String hlaDP2;

    @Column(name = "testing_date")
    private LocalDate testingDate;

    @Column(name = "laboratory_name")
    private String laboratoryName;

    @Column(name = "test_method")
    private String testMethod;

    @Column(name = "certification_number")
    private String certificationNumber;

    @Column(name = "hla_string")
    private String hlaString;

    @Column(name = "is_high_resolution")
    private Boolean isHighResolution;

    @Column(name = "resolution_level")
    private String resolutionLevel;
}