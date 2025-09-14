package com.recipientservice.model.history;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "recipient_history")
public class RecipientHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "recipient_snapshot_id")
    private RecipientSnapshotHistory recipientSnapshot;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "recipient_medical_details_snapshot_id")
    private RecipientMedicalDetailsSnapshotHistory medicalDetailsSnapshot;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "recipient_eligibility_criteria_snapshot_id")
    private RecipientEligibilityCriteriaSnapshotHistory eligibilityCriteriaSnapshot;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "hla_profile_snapshot_id")
    private RecipientHLAProfileSnapshotHistory hlaProfileSnapshot;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "receive_request_snapshot_id")
    private ReceiveRequestSnapshotHistory receiveRequestSnapshot;

    @Column(name = "match_id")
    private UUID matchId;

    @Column(name = "donation_id")
    private UUID donationId;

    @Column(name = "donor_user_id")
    private UUID donorUserId;

    @Column(name = "matched_at")
    private LocalDateTime matchedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
