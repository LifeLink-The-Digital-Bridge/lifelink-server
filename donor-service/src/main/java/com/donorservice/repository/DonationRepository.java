package com.donorservice.repository;

import com.donorservice.model.Donation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DonationRepository extends JpaRepository<Donation, Long> {
    List<Donation> findByDonorId(UUID donorId);
}
