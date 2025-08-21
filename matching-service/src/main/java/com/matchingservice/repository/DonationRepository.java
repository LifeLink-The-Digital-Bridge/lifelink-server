package com.matchingservice.repository;

import com.matchingservice.model.donor.Donation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DonationRepository extends JpaRepository<Donation, UUID> {
    Optional<Object> findByDonationId(UUID donationId);
}
