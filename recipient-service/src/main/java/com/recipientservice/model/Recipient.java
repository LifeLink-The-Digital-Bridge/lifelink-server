package com.recipientservice.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.recipientservice.enums.Availability;
import lombok.ToString;

@Data
@Entity
@Table(name = "recipients")
public class Recipient {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @ToString.Exclude
    private List<Location> addresses = new ArrayList<>();

    @OneToOne(mappedBy = "recipient", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private EligibilityCriteria eligibilityCriteria;

    @OneToOne(mappedBy = "recipient", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private MedicalDetails medicalDetails;

    @OneToOne(mappedBy = "recipient", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private ConsentForm consentForm;

    @OneToOne(mappedBy = "recipient", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private HLAProfile hlaProfile;

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<RecipientHistory> recipientHistories = new ArrayList<>();

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ReceiveRequest> receiveRequests = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Availability availability = Availability.AVAILABLE;

}
