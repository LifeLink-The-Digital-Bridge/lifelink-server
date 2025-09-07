package com.recipientservice.model.history;

import com.recipientservice.enums.Availability;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "recipient_snapshot_history")
public class RecipientSnapshotHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "original_recipient_id")
    private UUID originalRecipientId;

    @Column(name = "user_id")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability")
    private Availability availability;
}