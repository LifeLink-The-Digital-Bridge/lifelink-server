package com.matchingservice.model.recipients;

import com.matchingservice.enums.*;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data 
@Entity
@Table(name = "receive_request")
public class ReceiveRequest {

    @Id
    private UUID receiveRequestId;

    @Column(nullable = false)
    private UUID recipientId;

    @Column(nullable = false)
    private UUID userId;

    @Column
    private UUID locationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestType requestType;

    @Enumerated(EnumType.STRING)
    private BloodType requestedBloodType;

    @Enumerated(EnumType.STRING)
    private OrganType requestedOrgan;

    @Enumerated(EnumType.STRING)
    private TissueType requestedTissue;

    @Enumerated(EnumType.STRING)
    private StemCellType requestedStemCellType;

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
