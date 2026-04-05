package com.healthservice.service;

import com.healthservice.exception.ResourceNotFoundException;
import com.healthservice.kafka.HealthEventProducer;
import com.healthservice.model.ConnectionRequest;
import com.healthservice.model.DoctorPatientAssociation;
import com.healthservice.model.NGOMigrantAssociation;
import com.healthservice.repository.ConnectionRequestRepository;
import com.healthservice.repository.HealthIDRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ConnectionRequestService {

    @Autowired
    private ConnectionRequestRepository connectionRequestRepository;

    @Autowired
    private DoctorPatientAssociationService doctorPatientAssociationService;

    @Autowired
    private NGOMigrantAssociationService ngoMigrantAssociationService;

    @Autowired
    private HealthIDRepository healthIDRepository;

    @Autowired
    private HealthEventProducer healthEventProducer;

    @Transactional
    public ConnectionRequest sendConnectionRequest(UUID requesterUserId, String requesterRole,
                                                   UUID targetUserId, String targetRole,
                                                   String requestType, String message) {
        String normalizedRequesterRole = normalize(requesterRole);
        String normalizedTargetRole = normalize(targetRole);
        String normalizedRequestType = normalize(requestType);

        validateRequestParticipants(requesterUserId, targetUserId);
        validateRequestTypeAndRoles(normalizedRequesterRole, normalizedTargetRole, normalizedRequestType);

        if (areUsersConnected(requesterUserId, targetUserId, normalizedRequestType)) {
            throw new IllegalStateException("Users are already connected");
        }

        if (connectionRequestRepository.existsByRequesterUserIdAndTargetUserIdAndRequestTypeAndStatus(
                requesterUserId, targetUserId, normalizedRequestType, "PENDING")) {
            throw new IllegalStateException("Connection request already exists");
        }

        var existingReverse = connectionRequestRepository.findByRequesterUserIdAndTargetUserIdAndRequestType(
                targetUserId, requesterUserId, normalizedRequestType);
        
        if (existingReverse.isPresent() && existingReverse.get().getStatus().equals("PENDING")) {
            return acceptConnectionRequest(existingReverse.get().getId(), requesterUserId);
        }

        var request = new ConnectionRequest(requesterUserId, normalizedRequesterRole, targetUserId,
                                           normalizedTargetRole, normalizedRequestType, message);
        var savedRequest = connectionRequestRepository.save(request);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("requestId", savedRequest.getId().toString());
        eventData.put("requesterUserId", requesterUserId.toString());
        eventData.put("targetUserId", targetUserId.toString());
        eventData.put("requestType", normalizedRequestType);
        healthEventProducer.publishHealthEvent("health-record-events", eventData);

        return savedRequest;
    }

    @Transactional
    public ConnectionRequest acceptConnectionRequest(UUID requestId, UUID acceptingUserId) {
        var request = connectionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection request not found"));

        if (!request.getTargetUserId().equals(acceptingUserId)) {
            throw new IllegalStateException("Only the target user can accept this request");
        }

        if (!request.getStatus().equals("PENDING")) {
            throw new IllegalStateException("Request is not pending");
        }

        request.setStatus("ACCEPTED");
        request.setRespondedAt(LocalDateTime.now());
        var savedRequest = connectionRequestRepository.save(request);

        createAssociationFromRequest(request);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("requestId", requestId.toString());
        eventData.put("status", "ACCEPTED");
        eventData.put("requestType", request.getRequestType());
        healthEventProducer.publishHealthEvent("health-record-events", eventData);

        return savedRequest;
    }

    @Transactional
    public ConnectionRequest rejectConnectionRequest(UUID requestId, UUID rejectingUserId) {
        var request = connectionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection request not found"));

        if (!request.getTargetUserId().equals(rejectingUserId)) {
            throw new IllegalStateException("Only the target user can reject this request");
        }

        if (!request.getStatus().equals("PENDING")) {
            throw new IllegalStateException("Request is not pending");
        }

        request.setStatus("REJECTED");
        request.setRespondedAt(LocalDateTime.now());
        return connectionRequestRepository.save(request);
    }

    @Transactional
    public void cancelConnectionRequest(UUID requestId, UUID cancelingUserId) {
        var request = connectionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection request not found"));

        if (!request.getRequesterUserId().equals(cancelingUserId)) {
            throw new IllegalStateException("Only the requester can cancel this request");
        }

        if (!request.getStatus().equals("PENDING")) {
            throw new IllegalStateException("Can only cancel pending requests");
        }

        connectionRequestRepository.delete(request);
    }

    public List<ConnectionRequest> getIncomingRequests(UUID userId) {
        return connectionRequestRepository.findByTargetUserIdAndStatus(userId, "PENDING");
    }

    public List<ConnectionRequest> getSentRequests(UUID userId) {
        return connectionRequestRepository.findByRequesterUserIdAndStatus(userId, "PENDING");
    }

    public List<ConnectionRequest> getAcceptedConnections(UUID userId) {
        return connectionRequestRepository.findAcceptedConnectionsByUserId(userId);
    }

    public long getPendingRequestCount(UUID userId) {
        return connectionRequestRepository.countPendingRequestsForUser(userId);
    }

    public boolean areUsersConnected(UUID userId1, UUID userId2, String requestType) {
        var request1 = connectionRequestRepository.findByRequesterUserIdAndTargetUserIdAndRequestType(
                userId1, userId2, requestType);
        var request2 = connectionRequestRepository.findByRequesterUserIdAndTargetUserIdAndRequestType(
                userId2, userId1, requestType);
        
        return (request1.isPresent() && request1.get().getStatus().equals("ACCEPTED")) ||
               (request2.isPresent() && request2.get().getStatus().equals("ACCEPTED"));
    }

    private void createAssociationFromRequest(ConnectionRequest request) {
        if (request.getRequestType().equals("DOCTOR_PATIENT")) {
            UUID doctorId = request.getRequesterRole().equals("DOCTOR") 
                ? request.getRequesterUserId() : request.getTargetUserId();
            UUID patientId = request.getRequesterRole().equals("MIGRANT") 
                ? request.getRequesterUserId() : request.getTargetUserId();
            
            var healthId = healthIDRepository.findByUserId(patientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient Health ID not found"));
            
            doctorPatientAssociationService.createOrUpdateAssociation(
                    doctorId, healthId.getHealthId(), "Connection accepted");
                    
        } else if (request.getRequestType().equals("NGO_MIGRANT")) {
            UUID ngoId = request.getRequesterRole().equals("NGO") 
                ? request.getRequesterUserId() : request.getTargetUserId();
            UUID migrantId = request.getRequesterRole().equals("MIGRANT") 
                ? request.getRequesterUserId() : request.getTargetUserId();
            
            var healthId = healthIDRepository.findByUserId(migrantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Migrant Health ID not found"));
            
            ngoMigrantAssociationService.createAssociation(
                    ngoId, healthId.getHealthId(), "HEALTHCARE", "ACTIVE", "Connection accepted");
        }
    }

    private void validateRequestParticipants(UUID requesterUserId, UUID targetUserId) {
        if (requesterUserId == null || targetUserId == null) {
            throw new IllegalStateException("Requester and target user IDs are required");
        }
        if (requesterUserId.equals(targetUserId)) {
            throw new IllegalStateException("You cannot send a connection request to yourself");
        }
    }

    private void validateRequestTypeAndRoles(String requesterRole, String targetRole, String requestType) {
        if (requestType == null || requestType.isBlank()) {
            throw new IllegalStateException("Request type is required");
        }
        if (requesterRole == null || requesterRole.isBlank()) {
            throw new IllegalStateException("Requester role is required");
        }
        if (targetRole == null || targetRole.isBlank()) {
            throw new IllegalStateException("Target role is required");
        }

        if ("DOCTOR_PATIENT".equals(requestType)) {
            boolean requesterValid = "MIGRANT".equals(requesterRole) || "DOCTOR".equals(requesterRole);
            boolean targetValid = "MIGRANT".equals(targetRole) || "DOCTOR".equals(targetRole);
            boolean differentRoles = !requesterRole.equals(targetRole);
            if (!requesterValid || !targetValid || !differentRoles) {
                throw new IllegalStateException("DOCTOR_PATIENT requests must be between one migrant and one doctor");
            }
            return;
        }

        if ("NGO_MIGRANT".equals(requestType)) {
            boolean requesterValid = "MIGRANT".equals(requesterRole) || "NGO".equals(requesterRole);
            boolean targetValid = "MIGRANT".equals(targetRole) || "NGO".equals(targetRole);
            boolean differentRoles = !requesterRole.equals(targetRole);
            if (!requesterValid || !targetValid || !differentRoles) {
                throw new IllegalStateException("NGO_MIGRANT requests must be between one migrant and one NGO");
            }
            return;
        }

        throw new IllegalStateException("Unsupported request type: " + requestType);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }
}
