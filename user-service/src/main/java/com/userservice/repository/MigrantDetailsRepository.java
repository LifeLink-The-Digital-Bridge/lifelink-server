package com.userservice.repository;

import com.userservice.model.MigrantDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MigrantDetailsRepository extends JpaRepository<MigrantDetails, UUID> {
    
    Optional<MigrantDetails> findByUserId(UUID userId);
    
    boolean existsByUserId(UUID userId);
}
