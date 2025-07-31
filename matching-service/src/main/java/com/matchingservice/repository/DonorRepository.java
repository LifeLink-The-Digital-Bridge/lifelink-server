package com.matchingservice.repository;

import com.matchingservice.model.Donor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DonorRepository extends JpaRepository<Donor, UUID> {
}
