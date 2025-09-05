package com.matchingservice.repository;

import com.matchingservice.model.recipients.ReceiveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReceiveRequestRepository extends JpaRepository<ReceiveRequest, UUID> {

    Optional<ReceiveRequest> findByReceiveRequestId(UUID receiveRequestId);

    List<ReceiveRequest> findByStatus(com.matchingservice.enums.RequestStatus status);

    List<ReceiveRequest> findByStatusOrderByRequestDateDesc(com.matchingservice.enums.RequestStatus status);

    List<ReceiveRequest> findByRequestedBloodType(com.matchingservice.enums.BloodType requestedBloodType);

    List<ReceiveRequest> findByStatusAndRequestedBloodType(
            com.matchingservice.enums.RequestStatus status,
            com.matchingservice.enums.BloodType requestedBloodType
    );

    List<ReceiveRequest> findByRequestedOrgan(com.matchingservice.enums.OrganType requestedOrgan);

    List<ReceiveRequest> findByStatusAndRequestedOrgan(
            com.matchingservice.enums.RequestStatus status,
            com.matchingservice.enums.OrganType requestedOrgan
    );

    List<ReceiveRequest> findByUrgencyLevel(com.matchingservice.enums.UrgencyLevel urgencyLevel);

    List<ReceiveRequest> findByStatusAndUrgencyLevel(
            com.matchingservice.enums.RequestStatus status,
            com.matchingservice.enums.UrgencyLevel urgencyLevel
    );

    List<ReceiveRequest> findByRequestType(com.matchingservice.enums.RequestType requestType);

    List<ReceiveRequest> findByRequestDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT r FROM ReceiveRequest r WHERE r.requestDate >= :thirtyDaysAgo ORDER BY r.requestDate DESC")
    List<ReceiveRequest> findRecentRequests(@Param("thirtyDaysAgo") LocalDate thirtyDaysAgo);

    List<ReceiveRequest> findByRecipientId(UUID recipientId);

    long countByStatus(com.matchingservice.enums.RequestStatus status);

    @Query("SELECT r FROM ReceiveRequest r WHERE r.urgencyLevel IN ('EMERGENCY', 'URGENT') ORDER BY r.requestDate ASC")
    List<ReceiveRequest> findUrgentRequests();

}
