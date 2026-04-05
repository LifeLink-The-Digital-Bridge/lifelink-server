package com.healthservice.controller;

import com.healthservice.model.ConnectionRequest;
import com.healthservice.service.ConnectionRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/connection-requests")
@CrossOrigin(origins = "*")
public class ConnectionRequestController {

    @Autowired
    private ConnectionRequestService connectionRequestService;

    @PostMapping
    public ResponseEntity<ConnectionRequest> sendRequest(
            @RequestHeader(value = "id", required = false) UUID requesterUserId,
            @RequestHeader(value = "role", required = false) String roleHeader,
            @RequestBody Map<String, Object> request) {
        if (requesterUserId == null) {
            throw new IllegalStateException("Authenticated user id header is required");
        }

        String requesterRole = extractPrimaryRole(roleHeader);
        if (requesterRole == null || requesterRole.isBlank()) {
            throw new IllegalStateException("Authenticated user role header is required");
        }

        UUID targetUserId = parseUuidField(request.get("targetUserId"), "targetUserId");
        String targetRole = parseStringField(request.get("targetRole"), "targetRole", true);
        String requestType = parseStringField(request.get("requestType"), "requestType", true);
        String message = parseStringField(request.get("message"), "message", false);

        ConnectionRequest connectionRequest = connectionRequestService.sendConnectionRequest(
                requesterUserId, requesterRole, targetUserId, targetRole, requestType, message);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(connectionRequest);
    }

    @PutMapping("/{requestId}/accept")
    public ResponseEntity<ConnectionRequest> acceptRequest(
            @PathVariable UUID requestId,
            @RequestHeader(value = "id", required = false) UUID requesterUserId,
            @RequestBody Map<String, String> body) {
        if (requesterUserId == null) {
            throw new IllegalStateException("Authenticated user id header is required");
        }
        UUID acceptingUserId = parseUuidFromString(body.get("userId"), "userId");
        if (!requesterUserId.equals(acceptingUserId)) {
            throw new IllegalStateException("User id mismatch in request");
        }
        ConnectionRequest accepted = connectionRequestService.acceptConnectionRequest(requestId, acceptingUserId);
        return ResponseEntity.ok(accepted);
    }

    @PutMapping("/{requestId}/reject")
    public ResponseEntity<ConnectionRequest> rejectRequest(
            @PathVariable UUID requestId,
            @RequestHeader(value = "id", required = false) UUID requesterUserId,
            @RequestBody Map<String, String> body) {
        if (requesterUserId == null) {
            throw new IllegalStateException("Authenticated user id header is required");
        }
        UUID rejectingUserId = parseUuidFromString(body.get("userId"), "userId");
        if (!requesterUserId.equals(rejectingUserId)) {
            throw new IllegalStateException("User id mismatch in request");
        }
        ConnectionRequest rejected = connectionRequestService.rejectConnectionRequest(requestId, rejectingUserId);
        return ResponseEntity.ok(rejected);
    }

    @DeleteMapping("/{requestId}")
    public ResponseEntity<Void> cancelRequest(
            @PathVariable UUID requestId,
            @RequestParam UUID userId,
            @RequestHeader(value = "id", required = false) UUID requesterUserId) {
        if (requesterUserId == null) {
            throw new IllegalStateException("Authenticated user id header is required");
        }
        if (!requesterUserId.equals(userId)) {
            throw new IllegalStateException("User id mismatch in request");
        }
        connectionRequestService.cancelConnectionRequest(requestId, requesterUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/incoming/{userId}")
    public ResponseEntity<List<ConnectionRequest>> getIncomingRequests(
            @PathVariable UUID userId,
            @RequestHeader(value = "id", required = false) UUID requesterUserId) {
        if (requesterUserId == null || !requesterUserId.equals(userId)) {
            throw new IllegalStateException("You can only view your own incoming requests");
        }
        List<ConnectionRequest> requests = connectionRequestService.getIncomingRequests(userId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/sent/{userId}")
    public ResponseEntity<List<ConnectionRequest>> getSentRequests(
            @PathVariable UUID userId,
            @RequestHeader(value = "id", required = false) UUID requesterUserId) {
        if (requesterUserId == null || !requesterUserId.equals(userId)) {
            throw new IllegalStateException("You can only view your own sent requests");
        }
        List<ConnectionRequest> requests = connectionRequestService.getSentRequests(userId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/connections/{userId}")
    public ResponseEntity<List<ConnectionRequest>> getConnections(
            @PathVariable UUID userId,
            @RequestHeader(value = "id", required = false) UUID requesterUserId) {
        if (requesterUserId == null || !requesterUserId.equals(userId)) {
            throw new IllegalStateException("You can only view your own connections");
        }
        List<ConnectionRequest> connections = connectionRequestService.getAcceptedConnections(userId);
        return ResponseEntity.ok(connections);
    }

    @GetMapping("/pending-count/{userId}")
    public ResponseEntity<Map<String, Long>> getPendingCount(
            @PathVariable UUID userId,
            @RequestHeader(value = "id", required = false) UUID requesterUserId) {
        if (requesterUserId == null || !requesterUserId.equals(userId)) {
            throw new IllegalStateException("You can only view your own pending count");
        }
        long count = connectionRequestService.getPendingRequestCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/check-connection")
    public ResponseEntity<Map<String, Boolean>> checkConnection(
            @RequestParam UUID userId1,
            @RequestParam UUID userId2,
            @RequestParam String requestType) {
        boolean connected = connectionRequestService.areUsersConnected(userId1, userId2, requestType);
        return ResponseEntity.ok(Map.of("connected", connected));
    }

    private String extractPrimaryRole(String roleHeader) {
        if (roleHeader == null || roleHeader.isBlank()) {
            return null;
        }
        String normalized = roleHeader.trim().toUpperCase();
        if (!normalized.contains(",")) {
            return normalized;
        }
        String[] values = normalized.split(",");
        for (String value : values) {
            String role = value.trim();
            if (!role.isBlank()) {
                return role;
            }
        }
        return null;
    }

    private UUID parseUuidField(Object rawValue, String fieldName) {
        if (rawValue == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        String stringValue = String.valueOf(rawValue).trim();
        if (stringValue.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return UUID.fromString(stringValue);
    }

    private UUID parseUuidFromString(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return UUID.fromString(value.trim());
    }

    private String parseStringField(Object rawValue, String fieldName, boolean required) {
        if (rawValue == null) {
            if (required) {
                throw new IllegalArgumentException(fieldName + " is required");
            }
            return null;
        }
        String value = String.valueOf(rawValue).trim();
        if (required && value.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.isEmpty() ? null : value;
    }
}
