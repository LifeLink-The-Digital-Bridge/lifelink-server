package com.donorservice.repository;

import com.donorservice.model.history.DonorHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DonorHistoryRepository extends JpaRepository<DonorHistory, UUID> {

    boolean existsByMatchIdAndRecipientUserId(UUID matchId, UUID recipientUserId);

    List<DonorHistory> findByMatchId(UUID matchId);

    List<DonorHistory> findByRecipientUserId(UUID recipientUserId);

    List<DonorHistory> findByDonorUserIdAndRecipientUserId(UUID donorUserId, UUID recipientUserId);

    List<DonorHistory> findByDonorUserId(UUID userId);
}
