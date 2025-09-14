package com.recipientservice.model.history;

import com.recipientservice.enums.*;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "receive_request_snapshot_history")
public class ReceiveRequestSnapshotHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "original_request_id")
    private UUID originalRequestId;

    @Column(name = "recipient_id")
    private UUID recipientId;

    @Column(name = "recipient_user_id")
    private UUID recipientUserId;

    // UPDATED - Reference to normalized location instead of storing all fields
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "used_location_id")
    private RecipientLocationSnapshotHistory usedLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type")
    private RequestType requestType;

    @Enumerated(EnumType.STRING)
    @Column(name = "requested_blood_type")
    private BloodType requestedBloodType;

    @Enumerated(EnumType.STRING)
    @Column(name = "requested_organ")
    private OrganType requestedOrgan;

    @Enumerated(EnumType.STRING)
    @Column(name = "requested_tissue")
    private TissueType requestedTissue;

    @Enumerated(EnumType.STRING)
    @Column(name = "requested_stem_cell_type")
    private StemCellType requestedStemCellType;

    @Enumerated(EnumType.STRING)
    @Column(name = "urgency_level")
    private UrgencyLevel urgencyLevel;

    @Column(name = "quantity")
    private Double quantity;

    @Column(name = "request_date")
    private LocalDate requestDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RequestStatus status;

    @Column(name = "notes")
    private String notes;
}
