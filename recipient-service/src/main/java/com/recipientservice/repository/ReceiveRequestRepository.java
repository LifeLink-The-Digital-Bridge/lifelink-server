package com.recipientservice.repository;

import com.recipientservice.enums.RequestStatus;
import com.recipientservice.model.ReceiveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReceiveRequestRepository extends JpaRepository<ReceiveRequest, UUID> {

    List<ReceiveRequest> findAllByRecipientId(UUID recipientId);

    boolean existsByRecipientIdAndStatus(UUID recipientId, RequestStatus status);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM ReceiveRequest r WHERE r.recipient.id = :recipientId " +
            "AND r.status IN :statuses")
    boolean existsByRecipientIdAndStatusIn(
            @Param("recipientId") UUID recipientId,
            @Param("statuses") List<RequestStatus> statuses
    );

    @Query("SELECT r FROM ReceiveRequest r WHERE r.recipient.id = :recipientId " +
            "AND r.status IN :statuses")
    List<ReceiveRequest> findByRecipientIdAndStatusIn(
            @Param("recipientId") UUID recipientId,
            @Param("statuses") List<RequestStatus> statuses
    );

    @Query("SELECT COUNT(r) FROM ReceiveRequest r WHERE r.recipient.id = :recipientId " +
            "AND r.status IN :statuses")
    long countByRecipientIdAndStatusIn(
            @Param("recipientId") UUID recipientId,
            @Param("statuses") List<RequestStatus> statuses
    );
}
