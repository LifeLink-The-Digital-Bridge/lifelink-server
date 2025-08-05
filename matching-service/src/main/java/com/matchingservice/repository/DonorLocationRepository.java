package com.matchingservice.repository;

import com.matchingservice.model.DonorLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DonorLocationRepository extends JpaRepository<DonorLocation, Long> {
    Optional<DonorLocation> findByLocationId(Long locationId);
}

