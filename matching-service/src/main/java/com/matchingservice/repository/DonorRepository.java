package com.matchingservice.repository;

import com.matchingservice.model.donor.Donor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DonorRepository extends JpaRepository<Donor, UUID> {
    Optional<Donor> findByDonorId(UUID donorId);
}

