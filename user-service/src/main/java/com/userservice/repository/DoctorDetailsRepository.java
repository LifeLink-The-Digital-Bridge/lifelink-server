package com.userservice.repository;

import com.userservice.model.DoctorDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorDetailsRepository extends JpaRepository<DoctorDetails, UUID> {
    Optional<DoctorDetails> findByUserId(UUID userId);
    Optional<DoctorDetails> findByMedicalRegistrationNumber(String medicalRegistrationNumber);
    boolean existsByMedicalRegistrationNumber(String medicalRegistrationNumber);
}
