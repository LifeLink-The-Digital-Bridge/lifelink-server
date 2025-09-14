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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private UUID donationId;

    @Column(nullable = false)
    private UUID receiveRequestId;

    @Column(nullable = false)
    private UUID donorUserId;

    @Column(nullable = false)
    private UUID recipientUserId;

    @Column
    private UUID donorLocationId;

    @Column
    private UUID recipientLocationId;

    private Double distance;

    private Boolean isConfirmed = false;

    private Boolean donorConfirmed = false;

    private Boolean recipientConfirmed = false;

    private LocalDateTime donorConfirmedAt;

    private LocalDateTime recipientConfirmedAt;

    private LocalDateTime matchedAt;
}
