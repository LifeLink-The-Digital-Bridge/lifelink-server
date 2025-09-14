package com.recipientservice.repository;

import com.recipientservice.model.history.RecipientLocationSnapshotHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RecipientLocationSnapshotHistoryRepository extends JpaRepository<RecipientLocationSnapshotHistory, UUID> {
}
