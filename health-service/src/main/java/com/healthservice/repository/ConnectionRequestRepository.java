package com.healthservice.repository;

import com.healthservice.model.ConnectionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConnectionRequestRepository extends JpaRepository<ConnectionRequest, UUID> {

    List<ConnectionRequest> findByTargetUserIdAndStatus(UUID targetUserId, String status);

    List<ConnectionRequest> findByRequesterUserIdAndStatus(UUID requesterUserId, String status);

    List<ConnectionRequest> findByTargetUserId(UUID targetUserId);

    List<ConnectionRequest> findByRequesterUserId(UUID requesterUserId);

    @Query("SELECT c FROM ConnectionRequest c WHERE " +
           "(c.requesterUserId = :userId OR c.targetUserId = :userId) AND c.status = 'ACCEPTED'")
    List<ConnectionRequest> findAcceptedConnectionsByUserId(@Param("userId") UUID userId);

    Optional<ConnectionRequest> findByRequesterUserIdAndTargetUserIdAndRequestType(
        UUID requesterUserId, UUID targetUserId, String requestType);

    boolean existsByRequesterUserIdAndTargetUserIdAndRequestTypeAndStatus(
        UUID requesterUserId, UUID targetUserId, String requestType, String status);

    @Query("SELECT COUNT(c) FROM ConnectionRequest c WHERE c.targetUserId = :userId AND c.status = 'PENDING'")
    long countPendingRequestsForUser(@Param("userId") UUID userId);
}
