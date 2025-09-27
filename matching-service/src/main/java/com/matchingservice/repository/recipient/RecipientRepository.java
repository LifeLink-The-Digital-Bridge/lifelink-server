package com.matchingservice.repository.recipient;

import com.matchingservice.model.recipients.Recipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecipientRepository extends JpaRepository<Recipient, Long> {

    Optional<Recipient> findTopByUserIdOrderByEventTimestampDesc(UUID userId);

    Optional<Recipient> findTopByRecipientIdOrderByEventTimestampDesc(UUID recipientId);

    List<Recipient> findByRecipientIdOrderByEventTimestampDesc(UUID recipientId);

    @Query("SELECT r FROM Recipient r WHERE r.userId = :userId ORDER BY r.eventTimestamp DESC LIMIT 1")
    Optional<Recipient> findLatestByUserId(@Param("userId") UUID userId);

    @Query("""
        SELECT r FROM Recipient r WHERE r.eventTimestamp = (
            SELECT MAX(r2.eventTimestamp) FROM Recipient r2 WHERE r2.recipientId = r.recipientId
        )
    """)
    List<Recipient> findAllLatestVersions();
}
