package com.matchingservice.repository;

import com.matchingservice.enums.MatchStatus;
import com.matchingservice.model.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MatchResultRepository extends JpaRepository<MatchResult, UUID> {

    List<MatchResult> findByDonorUserIdOrderByMatchedAtDesc(UUID donorUserId);
    List<MatchResult> findByRecipientUserIdOrderByMatchedAtDesc(UUID recipientUserId);
    List<MatchResult> findByDonationIdOrderByCompatibilityScoreDesc(UUID donationId);
    List<MatchResult> findByReceiveRequestIdOrderByCompatibilityScoreDesc(UUID receiveRequestId);
    boolean existsByDonationIdAndRecipientUserId(UUID donationId, UUID recipientUserId);
    boolean existsByReceiveRequestIdAndDonorUserId(UUID receiveRequestId, UUID donorUserId);

    @Query("SELECT mr FROM MatchResult mr WHERE mr.donationId = :donationId AND mr.status != 'CANCELLED' AND mr.status != 'EXPIRED'")
    List<MatchResult> findActiveDonationMatches(@Param("donationId") UUID donationId);

    @Query("SELECT mr FROM MatchResult mr WHERE mr.receiveRequestId = :requestId AND mr.status != 'CANCELLED' AND mr.status != 'EXPIRED'")
    List<MatchResult> findActiveRequestMatches(@Param("requestId") UUID requestId);

    @Modifying
    @Query("UPDATE MatchResult mr SET mr.status = 'EXPIRED', mr.expiredAt = CURRENT_TIMESTAMP WHERE mr.donationId = :donationId AND mr.id != :excludeMatchId AND mr.status = 'PENDING'")
    void expirePendingDonationMatches(@Param("donationId") UUID donationId, @Param("excludeMatchId") UUID excludeMatchId);

    @Modifying
    @Query("UPDATE MatchResult mr SET mr.status = 'EXPIRED', mr.expiredAt = CURRENT_TIMESTAMP WHERE mr.receiveRequestId = :requestId AND mr.id != :excludeMatchId AND mr.status = 'PENDING'")
    void expirePendingRequestMatches(@Param("requestId") UUID requestId, @Param("excludeMatchId") UUID excludeMatchId);
}
