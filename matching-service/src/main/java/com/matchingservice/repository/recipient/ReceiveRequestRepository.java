package com.matchingservice.repository.recipient;

import com.matchingservice.enums.BloodType;
import com.matchingservice.enums.RequestStatus;
import com.matchingservice.enums.RequestType;
import com.matchingservice.model.recipients.ReceiveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReceiveRequestRepository extends JpaRepository<ReceiveRequest, UUID> {

    List<ReceiveRequest> findByRecipient_RecipientIdOrderByEventTimestampDesc(UUID recipientId);

    @Query("""
        SELECT rr FROM ReceiveRequest rr 
        WHERE rr.requestType = :requestType 
        AND rr.requestedBloodType = :bloodType 
        AND rr.status = :status
        ORDER BY rr.urgencyLevel DESC, rr.eventTimestamp DESC
    """)
    List<ReceiveRequest> findCompatibleRequests(
            @Param("requestType") RequestType requestType,
            @Param("bloodType") BloodType bloodType,
            @Param("status") RequestStatus status
    );

    @Query("""
        SELECT rr FROM ReceiveRequest rr 
        WHERE rr.status = :status 
        ORDER BY rr.urgencyLevel DESC, rr.requestDate ASC
    """)
    List<ReceiveRequest> findByStatusOrderByUrgencyAndDate(@Param("status") RequestStatus status);

    @Query("SELECT rr FROM ReceiveRequest rr WHERE rr.recipient.userId = :userId ORDER BY rr.eventTimestamp DESC")
    List<ReceiveRequest> findByUserIdOrderByEventTimestampDesc(@Param("userId") UUID userId);

    List<ReceiveRequest> findByStatus(RequestStatus status);

    List<ReceiveRequest> findByStatusIn(List<RequestStatus> statuses);
}
