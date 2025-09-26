package com.matchingservice.model.recipients;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.matchingservice.enums.Availability;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "recipients")
public class Recipient {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private UUID recipientId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime eventTimestamp;

    @Enumerated(EnumType.STRING)
    private Availability availability = Availability.AVAILABLE;

    @OneToOne(mappedBy = "recipient", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private RecipientEligibilityCriteria eligibilityCriteria;

    @OneToOne(mappedBy = "recipient", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private RecipientMedicalDetails medicalDetails;

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @ToString.Exclude
    private List<RecipientLocation> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ReceiveRequest> receiveRequests = new ArrayList<>();
}
