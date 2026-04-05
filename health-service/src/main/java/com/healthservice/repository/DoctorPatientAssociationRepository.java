package com.healthservice.repository;

import com.healthservice.model.DoctorPatientAssociation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorPatientAssociationRepository extends JpaRepository<DoctorPatientAssociation, UUID> {

    Optional<DoctorPatientAssociation> findByDoctorIdAndPatientHealthId(UUID doctorId, String patientHealthId);

    List<DoctorPatientAssociation> findByDoctorIdAndIsActiveTrue(UUID doctorId);

    Page<DoctorPatientAssociation> findByDoctorIdAndIsActiveTrue(UUID doctorId, Pageable pageable);

    List<DoctorPatientAssociation> findByPatientHealthIdAndIsActiveTrue(String patientHealthId);

    List<DoctorPatientAssociation> findByPatientUserIdAndIsActiveTrue(UUID patientUserId);

    @Query("SELECT COUNT(d) FROM DoctorPatientAssociation d WHERE d.doctorId = :doctorId AND d.isActive = true")
    long countActivePatientsForDoctor(@Param("doctorId") UUID doctorId);

    long countByIsActiveTrue();

    boolean existsByDoctorIdAndPatientHealthId(UUID doctorId, String patientHealthId);

    boolean existsByDoctorIdAndPatientUserIdAndIsActiveTrue(UUID doctorId, UUID patientUserId);
}
