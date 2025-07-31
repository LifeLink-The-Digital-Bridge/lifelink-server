package com.matchingservice.repository;

import com.matchingservice.model.DonorLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonorLocationRepository extends JpaRepository<DonorLocation, Long> {
}
