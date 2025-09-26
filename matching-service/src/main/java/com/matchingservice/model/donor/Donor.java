package com.matchingservice.model.donor;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.matchingservice.enums.DonorStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "donors")
@Data
public class Donor {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private UUID donorId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private LocalDate registrationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DonorStatus status;

    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime eventTimestamp;

    @OneToOne(mappedBy = "donor", cascade = CascadeType.ALL)
    private DonorMedicalDetails medicalDetails;

    @OneToOne(mappedBy = "donor", cascade = CascadeType.ALL)
    private DonorEligibilityCriteria eligibilityCriteria;

    @OneToMany(mappedBy = "donor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @ToString.Exclude
    private List<DonorLocation> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "donor", cascade = CascadeType.ALL)
    private List<Donation> donations = new ArrayList<>();
}
