package com.recipientservice.repository;

import com.recipientservice.model.history.RecipientHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecipientHistoryRepository extends JpaRepository<RecipientHistory, UUID> {

    boolean existsByMatchIdAndDonorUserId(UUID matchId, UUID donorUserId);

    List<RecipientHistory> findByMatchId(UUID matchId);

    List<RecipientHistory> findByDonorUserId(UUID donorUserId);

    List<RecipientHistory> findByRecipientUserId(UUID recipientUserId);

    List<RecipientHistory> findByRecipientUserIdAndDonorUserId(UUID recipientUserId, UUID donorUserId);

    List<RecipientHistory> findByRecipientSnapshot_UserId(UUID userId);

    List<RecipientHistory> findByRecipientSnapshot_UserIdAndDonorUserId(UUID recipientUserId, UUID donorUserId);
}