package com.matchingservice.repository;

import com.matchingservice.enums.MatchStatus;
import com.matchingservice.model.MatchResult;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
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
    @Query("UPDATE MatchResult mr SET mr.status = 'EXPIRED', " + "mr.expiredAt = CURRENT_TIMESTAMP, " + "mr.expiryReason = 'MATCH_CONFIRMED_ELSEWHERE' " + "WHERE mr.donationId = :donationId AND mr.id != :excludeMatchId " + "AND mr.status IN ('PENDING', 'DONOR_CONFIRMED', 'RECIPIENT_CONFIRMED')")
    void expirePendingDonationMatches(@Param("donationId") UUID donationId, @Param("excludeMatchId") UUID excludeMatchId);

    @Modifying
    @Query("UPDATE MatchResult mr SET mr.status = 'EXPIRED', " + "mr.expiredAt = CURRENT_TIMESTAMP, " + "mr.expiryReason = 'MATCH_CONFIRMED_ELSEWHERE' " + "WHERE mr.receiveRequestId = :requestId AND mr.id != :excludeMatchId " + "AND mr.status IN ('PENDING', 'DONOR_CONFIRMED', 'RECIPIENT_CONFIRMED')")
    void expirePendingRequestMatches(@Param("requestId") UUID requestId, @Param("excludeMatchId") UUID excludeMatchId);

    @Query("SELECT m FROM MatchResult m WHERE m.donationId = :donationId " + "AND m.status IN :statuses")
    List<MatchResult> findByDonationIdAndStatusIn(@Param("donationId") UUID donationId, @Param("statuses") List<MatchStatus> statuses);

    @Query("SELECT m FROM MatchResult m WHERE m.receiveRequestId = :requestId " + "AND m.status IN :statuses")
    List<MatchResult> findByReceiveRequestIdAndStatusIn(@Param("requestId") UUID requestId, @Param("statuses") List<MatchStatus> statuses);

    @Modifying
    @Query("UPDATE MatchResult m SET " + "m.status = 'CANCELLED_BY_DONOR', " + "m.expiryReason = :reason, " + "m.expiredAt = CURRENT_TIMESTAMP " + "WHERE m.donationId = :donationId " + "AND m.status IN ('PENDING', 'DONOR_CONFIRMED', 'RECIPIENT_CONFIRMED')")
    int expireMatchesForCancelledDonation(@Param("donationId") UUID donationId, @Param("reason") String reason);

    @Modifying
    @Query("UPDATE MatchResult m SET " + "m.status = 'CANCELLED_BY_RECIPIENT', " + "m.expiryReason = :reason, " + "m.expiredAt = CURRENT_TIMESTAMP " + "WHERE m.receiveRequestId = :requestId " + "AND m.status IN ('PENDING', 'DONOR_CONFIRMED', 'RECIPIENT_CONFIRMED')")
    int expireMatchesForCancelledRequest(@Param("requestId") UUID requestId, @Param("reason") String reason);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM MatchResult m WHERE m.id = :id")
    Optional<MatchResult> findByIdWithLock(@Param("id") UUID id);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END " + "FROM MatchResult m WHERE m.donationId = :donationId " + "AND m.status = 'CONFIRMED'")
    boolean existsConfirmedMatchByDonationId(@Param("donationId") UUID donationId);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END " + "FROM MatchResult m WHERE m.receiveRequestId = :requestId " + "AND m.status = 'CONFIRMED'")
    boolean existsConfirmedMatchByRequestId(@Param("requestId") UUID requestId);
}
