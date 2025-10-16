package com.matchingservice.model.recipients;

import com.matchingservice.enums.*;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "receive_requests")
public class ReceiveRequest {

    @Id
    private UUID receiveRequestId;

    @ManyToOne
    @JoinColumn(name = "recipient_db_id", referencedColumnName = "id")
    private Recipient recipient;

    @Column(nullable = false)
    private UUID recipientId;

    @ManyToOne
    @JoinColumn(name = "location_db_id", referencedColumnName = "id")
    private RecipientLocation location;

    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime eventTimestamp;

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
    @Column(nullable = false)
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
