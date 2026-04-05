package com.healthservice.repository;

import com.healthservice.model.HealthRecordComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HealthRecordCommentRepository extends JpaRepository<HealthRecordComment, UUID> {

    List<HealthRecordComment> findByHealthRecordIdOrderByCreatedAtAsc(UUID healthRecordId);
}
