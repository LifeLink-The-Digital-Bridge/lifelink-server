package com.matchingservice.repository;

import com.matchingservice.model.donor.DonorLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DonorLocationRepository extends JpaRepository<DonorLocation, UUID> {
    List<DonorLocation> findByDonorId(UUID donorId);
}

