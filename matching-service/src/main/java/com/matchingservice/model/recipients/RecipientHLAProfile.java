package com.matchingservice.model.recipients;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "recipient_hla_profiles")
public class RecipientHLAProfile {
    @Id
    private Long id;

    @Column(nullable = false)
    private UUID recipientId;

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
