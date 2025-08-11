package com.matchingservice.repository;

import com.matchingservice.model.recipients.RecipientLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecipientLocationRepository extends JpaRepository<RecipientLocation, UUID> {
    Optional<RecipientLocation> findByLocationId(UUID locationId);
}
