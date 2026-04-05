package com.healthservice.repository;

import com.healthservice.enums.RecordType;
import com.healthservice.model.HealthRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface HealthRecordRepository extends JpaRepository<HealthRecord, UUID> {
    
    List<HealthRecord> findByUserIdOrderByRecordDateDesc(UUID userId);
    
    List<HealthRecord> findByHealthIdOrderByRecordDateDesc(String healthId);
    
    Page<HealthRecord> findByUserId(UUID userId, Pageable pageable);
    
    List<HealthRecord> findByUserIdAndRecordType(UUID userId, RecordType recordType);
    
    List<HealthRecord> findByUserIdAndRecordDateBetween(UUID userId, LocalDate startDate, LocalDate endDate);
    
    List<HealthRecord> findByUserIdAndIsEmergencyTrue(UUID userId);

    List<HealthRecord> findByUserIdInAndIsEmergencyTrueOrderByRecordDateDesc(List<UUID> userIds);
    
    long countByUserId(UUID userId);

    long countByIsEmergencyTrue();

    long countByCreatedAtAfter(LocalDateTime threshold);
}
