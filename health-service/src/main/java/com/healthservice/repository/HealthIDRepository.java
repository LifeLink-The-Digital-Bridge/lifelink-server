package com.healthservice.repository;

import com.healthservice.model.HealthID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HealthIDRepository extends JpaRepository<HealthID, UUID> {
    
    Optional<HealthID> findByHealthId(String healthId);
    
    Optional<HealthID> findByUserId(UUID userId);
    
    boolean existsByHealthId(String healthId);
    
    boolean existsByUserId(UUID userId);
}
