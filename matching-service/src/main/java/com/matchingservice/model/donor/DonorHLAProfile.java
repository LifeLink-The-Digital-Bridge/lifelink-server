package com.matchingservice.model.donor;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "donor_hla_profiles")
public class DonorHLAProfile {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long dbId;

    @Column
    private Long id;

    @ManyToOne
    @JoinColumn(name = "donor_db_id", referencedColumnName = "id")
    private Donor donor;

    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime eventTimestamp;

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
    private String hlaDR1;

    @Column(name = "hla_dr2")
    private String hlaDR2;

    @Column(name = "hla_dq1")
    private String hlaDQ1;

    @Column(name = "hla_dq2")
    private String hlaDQ2;

    @Column(name = "hla_dp1")
    private String hlaDP1;

    @Column(name = "hla_dp2")
    private String hlaDP2;

    @Column(nullable = false)
    private LocalDate testingDate;

    @Column
    private String testingMethod;

    @Column
    private String laboratoryName;

    @Column
    private String certificationNumber;

    @Column
    private String hlaString;

    @Column
    private Boolean isHighResolution;
}
