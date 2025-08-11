package com.matchingservice.repository;

import com.matchingservice.model.recipients.ReceiveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReceiveRequestRepository extends JpaRepository<ReceiveRequest, UUID> {
    Optional<ReceiveRequest> findByReceiveRequestId(UUID receiveRequestId);
}
