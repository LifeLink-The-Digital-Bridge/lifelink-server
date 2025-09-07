package com.recipientservice.repository;

import com.recipientservice.model.RecipientHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecipientHistoryRepository extends JpaRepository<RecipientHistory, UUID> {
    List<RecipientHistory> findByRecipientSnapshot_UserId(UUID userId);
}