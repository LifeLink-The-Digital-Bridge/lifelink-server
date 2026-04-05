package com.healthservice.repository;

import com.healthservice.model.MigrantProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MigrantProfileRepository extends JpaRepository<MigrantProfile, UUID> {
    
    Optional<MigrantProfile> findByUserId(UUID userId);
    
    Optional<MigrantProfile> findByHealthId(String healthId);
    
    boolean existsByUserId(UUID userId);
}
