package com.donorservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "consent_form")
public class ConsentForm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "donor_id", referencedColumnName = "id")
    private Donor donor;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private Boolean isConsented;

    @Column(nullable = false)
    private LocalDateTime consentedAt;
}
