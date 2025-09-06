package com.matchingservice.repository;

import com.matchingservice.model.donor.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MatchResultRepository extends JpaRepository<MatchResult, UUID> {
    List<MatchResult> findByDonationId(UUID donationId);
    List<MatchResult> findByReceiveRequestId(UUID receiveRequestId);
    List<MatchResult> findByDonorUserId(UUID donorUserId);
    List<MatchResult> findByRecipientUserId(UUID recipientUserId);
}
