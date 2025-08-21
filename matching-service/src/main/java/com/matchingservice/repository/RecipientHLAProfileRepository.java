package com.matchingservice.repository;

import com.matchingservice.model.recipients.Recipient;
import com.matchingservice.model.recipients.RecipientHLAProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecipientHLAProfileRepository extends JpaRepository<RecipientHLAProfile, Long> {

    Optional<RecipientHLAProfile> findByRecipient(Recipient recipient);

    Optional<RecipientHLAProfile> findByRecipient_RecipientId(UUID recipientId);

    boolean existsByRecipient(Recipient recipient);

    void deleteByRecipient(Recipient recipient);
}

