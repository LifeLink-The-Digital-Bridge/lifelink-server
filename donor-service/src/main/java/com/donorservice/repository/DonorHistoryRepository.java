package com.donorservice.repository;

import com.donorservice.model.history.DonorHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DonorHistoryRepository extends JpaRepository<DonorHistory, UUID> {
    List<DonorHistory> findByDonorSnapshot_UserId(UUID userId);
}