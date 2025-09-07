package com.matchingservice.repository;

import com.matchingservice.model.donor.Donor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DonorRepository extends JpaRepository<Donor, UUID> {
    Optional<Donor> findByUserId(UUID userId);
}