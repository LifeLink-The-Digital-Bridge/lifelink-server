package com.matchingservice.model.recipients;

import com.matchingservice.enums.BloodType;
import com.matchingservice.enums.OrganType;
import com.matchingservice.enums.RequestStatus;
import com.matchingservice.enums.UrgencyLevel;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "receive_request_events")
public class ReceiveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID receiveRequestId;

    @Column(nullable = false)
    private UUID recipientId;

    @Enumerated(EnumType.STRING)
    private BloodType requestedBloodType;

    @Enumerated(EnumType.STRING)
    private OrganType requestedOrgan;

    @Enumerated(EnumType.STRING)
    private UrgencyLevel urgencyLevel;

    @Column(nullable = false)
    private Double quantity;

    @Column(nullable = false)
    private LocalDate requestDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    @Column
    private String notes;
}

