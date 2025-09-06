package com.donorservice.model;

import com.donorservice.enums.DonorStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "donors")
public class Donor {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private LocalDate registrationDate;

    @OneToMany(mappedBy = "donor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @ToString.Exclude
    private List<Location> addresses = new ArrayList<>();

    @Column(nullable = false)
    private DonorStatus status;

    @OneToOne(mappedBy = "donor", cascade = CascadeType.ALL)
    private MedicalDetails medicalDetails;

    @OneToOne(mappedBy = "donor", cascade = CascadeType.ALL)
    private EligibilityCriteria eligibilityCriteria;

    @OneToOne(mappedBy = "donor", cascade = CascadeType.ALL)
    private ConsentForm consentForm;

    @OneToOne(mappedBy = "donor", cascade = CascadeType.ALL)
    private HLAProfile hlaProfile;

    @OneToMany(mappedBy = "donor", cascade = CascadeType.ALL)
    private List<DonationHistory> donationHistory;

    @OneToMany(mappedBy = "donor", cascade = CascadeType.ALL)
    private List<Donation> donations;

    @PrePersist
    public void setRegistrationDate() {
        if (registrationDate == null) {
            registrationDate = LocalDate.now();
        }
    }
}
