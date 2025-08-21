package com.matchingservice.model.donor;

import com.matchingservice.model.recipients.ReceiveRequest;
import com.matchingservice.model.recipients.RecipientLocation;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "match_results")
@Data
public class MatchResult {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "donation_id", nullable = false)
    private Donation donation;

    @ManyToOne
    @JoinColumn(name = "receive_request_id", nullable = false)
    private ReceiveRequest receiveRequest;

    private Double distance;

    private Boolean isConfirmed = false;

    private LocalDateTime matchedAt;

    @ManyToOne
    @JoinColumn(name = "donor_location_id")
    private DonorLocation donorLocation;

    @ManyToOne
    @JoinColumn(name = "recipient_location_id")
    private RecipientLocation recipientLocation;
}
