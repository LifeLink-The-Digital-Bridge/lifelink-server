package com.healthservice.repository;

import com.healthservice.model.MigrantRiskScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MigrantRiskScoreRepository extends JpaRepository<MigrantRiskScore, UUID> {
    Optional<MigrantRiskScore> findByUserId(UUID userId);
}
