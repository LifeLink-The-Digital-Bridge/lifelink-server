package com.matchingservice.repository.recipient;

import com.matchingservice.model.recipients.RecipientHLAProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecipientHLAProfileRepository extends JpaRepository<RecipientHLAProfile, Long> {

    Optional<RecipientHLAProfile> findTopByRecipient_RecipientIdOrderByEventTimestampDesc(UUID recipientId);

    @Query("SELECT rhp FROM RecipientHLAProfile rhp WHERE rhp.recipient.recipientId = :recipientId ORDER BY rhp.eventTimestamp DESC")
    List<RecipientHLAProfile> findAllByRecipientIdOrderByEventTimestampDesc(@Param("recipientId") UUID recipientId);

    @Query("""
        SELECT rhp FROM RecipientHLAProfile rhp 
        WHERE rhp.hlaA1 = :hlaA1 OR rhp.hlaA2 = :hlaA1
        OR rhp.hlaB1 = :hlaB1 OR rhp.hlaB2 = :hlaB1
        ORDER BY rhp.eventTimestamp DESC
    """)
    List<RecipientHLAProfile> findByHLAMarkers(
            @Param("hlaA1") String hlaA1,
            @Param("hlaB1") String hlaB1
    );

    Optional<RecipientHLAProfile> findTopByRecipient_RecipientIdAndIdOrderByEventTimestampDesc(UUID recipientId, Long id);
}
