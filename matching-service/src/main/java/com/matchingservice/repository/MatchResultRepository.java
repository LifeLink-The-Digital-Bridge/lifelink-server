package com.matchingservice.repository;

import com.matchingservice.model.donor.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MatchResultRepository extends JpaRepository<MatchResult, UUID> {

    List<MatchResult> findByIsConfirmedTrue();
    List<MatchResult> findByIsConfirmedFalse();

    List<MatchResult> findByDonationDonorUserId(UUID donorUserId);
    List<MatchResult> findByDonationDonationId(UUID donationId);
    List<MatchResult> findByReceiveRequestRecipientId(UUID recipientUserId);
    List<MatchResult> findByReceiveRequestReceiveRequestId(UUID receiveRequestId);
}
