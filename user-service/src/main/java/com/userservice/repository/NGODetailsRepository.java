package com.userservice.repository;

import com.userservice.model.NGODetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NGODetailsRepository extends JpaRepository<NGODetails, UUID> {
    Optional<NGODetails> findByUserId(UUID userId);
    Optional<NGODetails> findByRegistrationNumber(String registrationNumber);
    boolean existsByRegistrationNumber(String registrationNumber);
}
