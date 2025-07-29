package com.matchingservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "match_results")
@Data
public class MatchResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID donorId;
    private UUID recipientId;

    private Double distance;

    private Boolean isConfirmed = false;

    private LocalDateTime matchedAt;

    @ManyToOne
    @JoinColumn(name = "donor_location_id")
    private Location donorLocation;

    @ManyToOne
    @JoinColumn(name = "recipient_location_id")
    private Location recipientLocation;
}

