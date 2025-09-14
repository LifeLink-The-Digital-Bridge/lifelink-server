package com.donorservice.model.history;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "donor_history")
public class DonorHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "donor_snapshot_id")
    private DonorSnapshotHistory donorSnapshot;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "hla_profile_snapshot_id")
    private HLAProfileSnapshotHistory hlaProfileSnapshot;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "eligibility_criteria_snapshot_id")
    private EligibilityCriteriaSnapshotHistory eligibilityCriteriaSnapshot;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "medical_details_snapshot_id")
    private MedicalDetailsSnapshotHistory medicalDetailsSnapshot;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "donation_snapshot_id")
    private DonationSnapshotHistory donationSnapshot;

    @Column(name = "match_id")
    private UUID matchId;

    @Column(name = "receive_request_id")
    private UUID receiveRequestId;

    @Column(name = "recipient_user_id")
    private UUID recipientUserId;

    @Column(name = "matched_at")
    private LocalDateTime matchedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
