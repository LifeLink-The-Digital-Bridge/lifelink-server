package com.matchingservice.repository;

import com.matchingservice.model.donor.Donor;
import com.matchingservice.model.donor.DonorHLAProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DonorHLAProfileRepository extends JpaRepository<DonorHLAProfile, Long> {
    Optional<DonorHLAProfile> findByDonorId(UUID donorId);
}

