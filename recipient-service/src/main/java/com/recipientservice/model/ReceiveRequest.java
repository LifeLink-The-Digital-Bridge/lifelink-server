package com.recipientservice.model;

import com.recipientservice.enums.*;
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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "recipient_id", referencedColumnName = "id")
    private Recipient recipient;

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

    @ManyToOne
    @JoinColumn(name = "location_id", referencedColumnName = "id")
    private Location location;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancelled_by_user_id")
    private UUID cancelledByUserId;

    @Column(name = "additional_cancellation_notes", length = 1000)
    private String additionalCancellationNotes;
}
