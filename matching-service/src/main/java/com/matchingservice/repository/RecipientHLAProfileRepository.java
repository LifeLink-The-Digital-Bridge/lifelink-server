package com.matchingservice.repository;

import com.matchingservice.model.recipients.RecipientHLAProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecipientHLAProfileRepository extends JpaRepository<RecipientHLAProfile, Long> {
    Optional<RecipientHLAProfile> findByRecipientId(UUID recipientId);
}