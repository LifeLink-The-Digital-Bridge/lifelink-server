package com.matchingservice.repository;

import com.matchingservice.model.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MatchResultRepository extends JpaRepository<MatchResult, UUID> {

    boolean existsByDonationIdAndReceiveRequestId(UUID donationId, UUID receiveRequestId);

    List<MatchResult> findByDonationIdOrderByCompatibilityScoreDesc(UUID donationId);

    List<MatchResult> findByReceiveRequestIdOrderByCompatibilityScoreDesc(UUID receiveRequestId);

    List<MatchResult> findByDonorUserIdOrderByMatchedAtDesc(UUID donorUserId);

    List<MatchResult> findByRecipientUserIdOrderByMatchedAtDesc(UUID recipientUserId);

    @Query("""
        SELECT mr FROM MatchResult mr 
        WHERE mr.compatibilityScore >= :minScore 
        ORDER BY mr.compatibilityScore DESC, mr.matchedAt DESC
    """)
    List<MatchResult> findTopMatchesByScore(@Param("minScore") Double minScore);

    List<MatchResult> findByIsConfirmedTrueOrderByMatchedAtDesc();

    @Query("""
        SELECT mr FROM MatchResult mr 
        WHERE mr.donorUserId = :donorUserId 
        AND mr.isConfirmed = false 
        ORDER BY mr.compatibilityScore DESC
    """)
    List<MatchResult> findPendingMatchesForDonor(@Param("donorUserId") UUID donorUserId);

    @Query("""
        SELECT mr FROM MatchResult mr 
        WHERE mr.recipientUserId = :recipientUserId 
        AND mr.isConfirmed = false 
        ORDER BY mr.compatibilityScore DESC
    """)
    List<MatchResult> findPendingMatchesForRecipient(@Param("recipientUserId") UUID recipientUserId);

    boolean existsByDonationIdAndRecipientUserId(UUID donationId, UUID recipientUserId);

    boolean existsByReceiveRequestIdAndDonorUserId(UUID requestId, UUID donorUserId);
}
