package com.recipientservice.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import java.util.UUID;
import com.recipientservice.enums.Availability;

@Data
@Entity
@Table(name = "recipients")
public class Recipient {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne
    @JoinColumn(name = "location_id", referencedColumnName = "id")
    private Location location;

    @OneToOne(mappedBy = "recipient", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private EligibilityCriteria eligibilityCriteria;

    @OneToOne(mappedBy = "recipient", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private MedicalDetails medicalDetails;

    @OneToOne(mappedBy = "recipient", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private ConsentForm consentForm;

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<RecipientHistory> recipientHistories;

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ReceiveRequest> receiveRequests;

    @Enumerated(EnumType.STRING)
    private Availability availability;

    public Recipient() {
        this.availability = Availability.AVAILABLE;
    }
}
