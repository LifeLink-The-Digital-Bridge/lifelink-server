package com.matchingservice.repository;

import com.matchingservice.model.MatchResult;
import com.matchingservice.model.donor.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface MatchResultRepository extends JpaRepository<MatchResult, UUID> {
    List<MatchResult> findByDonationId(UUID donationId);
    List<MatchResult> findByReceiveRequestId(UUID receiveRequestId);
    List<MatchResult> findByDonorUserId(UUID donorUserId);
    List<MatchResult> findByRecipientUserId(UUID recipientUserId);

    List<MatchResult> findByDonationIdAndRecipientUserId(UUID donationId, UUID recipientUserId);
    List<MatchResult> findByReceiveRequestIdAndDonorUserId(UUID requestId, UUID donorUserId);
}
