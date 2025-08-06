package com.matchingservice.repository;

import com.matchingservice.model.DonorLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DonorLocationRepository extends JpaRepository<DonorLocation, UUID> {
    Optional<DonorLocation> findByLocationId(UUID locationId);
}

