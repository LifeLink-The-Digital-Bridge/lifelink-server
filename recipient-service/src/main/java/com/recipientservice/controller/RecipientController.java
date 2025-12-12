package com.recipientservice.controller;

import com.recipientservice.aop.InternalOnly;
import com.recipientservice.aop.RequireRole;
import com.recipientservice.client.UserClient;
import com.recipientservice.dto.*;
import com.recipientservice.enums.RequestStatus;
import com.recipientservice.exceptions.AccessDeniedException;
import com.recipientservice.model.Recipient;
import com.recipientservice.repository.RecipientRepository;
import com.recipientservice.service.RecipientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/recipients")
public class RecipientController {

    private final UserClient userClient;
    private final RecipientService recipientService;
    private final RecipientRepository recipientRepository;

    public RecipientController(UserClient userClient, RecipientService recipientService, RecipientRepository recipientRepository) {
        this.userClient = userClient;
        this.recipientService = recipientService;
        this.recipientRepository = recipientRepository;
    }

    @PutMapping("/addRole")
    public ResponseEntity<String> addRoleToUser(@RequestHeader("id") String userId) {
        userClient.addRole(UUID.fromString(userId), "RECIPIENT");
        return ResponseEntity.ok("Role added");
    }

    @RequireRole("RECIPIENT")
    @PostMapping("/profile")
    public ResponseEntity<?> saveOrUpdateRecipient(@RequestHeader("id") UUID userId, @RequestBody RegisterRecipientDTO recipientDTO) {
        try {
            RecipientDTO result = recipientService.saveOrUpdateRecipient(userId, recipientDTO);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @RequireRole("RECIPIENT")
    @PostMapping("/request")
    public ResponseEntity<ReceiveRequestDTO> createReceiveRequest(@RequestHeader("id") UUID userId, @RequestBody CreateReceiveRequestDTO requestDTO) {
        ReceiveRequestDTO response = recipientService.createReceiveRequest(userId, requestDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-userId")
    public ResponseEntity<RecipientDTO> getRecipientByUserId(@RequestHeader("id") UUID userId) {
        RecipientDTO recipient = recipientService.getRecipientByUserId(userId);
        if (recipient == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(recipient);
    }

    @GetMapping("/by-userId/{userId}")
    public ResponseEntity<RecipientDTO> getRecipientByUser(@PathVariable UUID userId) {
        RecipientDTO recipient = recipientService.getRecipientByUserId(userId);
        if (recipient == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(recipient);
    }

    @RequireRole("RECIPIENT")
    @GetMapping("/{id}")
    public RecipientDTO getRecipient(@PathVariable UUID id) {
        return recipientService.getRecipientById(id);
    }

    @InternalOnly
    @GetMapping("/internal/{id}")
    public ResponseEntity<RecipientDTO> getRecipientByIdInternal(@PathVariable UUID id) {
        RecipientDTO recipient = recipientService.getRecipientById(id);
        if (recipient == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(recipient);
    }

    @GetMapping("/{recipientId}/requests")
    public ResponseEntity<?> getReceiveRequests(@PathVariable UUID recipientId, @RequestHeader("id") String requesterId) {

        try {
            UUID requesterUUID = UUID.fromString(requesterId);
            return ResponseEntity.ok(recipientService.getReceiveRequestsByRecipientId(recipientId, requesterUUID));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @RequireRole("RECIPIENT")
    @GetMapping("/my-requests")
    public ResponseEntity<List<ReceiveRequestDTO>> getMyRequests(@RequestHeader("id") UUID userId) {
        return ResponseEntity.ok(recipientService.getReceiveRequestsByUserId(userId));
    }

    @GetMapping("/by-userId/{userId}/requests")
    public ResponseEntity<?> getRequestsByUserId(@PathVariable UUID userId, @RequestHeader("id") String requesterId) {

        try {
            UUID requesterUUID = UUID.fromString(requesterId);

            Recipient recipient = recipientRepository.findByUserId(userId);

            if (recipient == null) {
                return ResponseEntity.ok(List.of());
            }

            List<ReceiveRequestDTO> requests = recipientService.getReceiveRequestsByRecipientId(recipient.getId(), requesterUUID);
            return ResponseEntity.ok(requests);

        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @InternalOnly
    @PutMapping("/requests/{requestId}/status")
    public ResponseEntity<String> updateRequestStatus(@PathVariable UUID requestId, @RequestBody RequestStatus status) {
        recipientService.updateRequestStatus(requestId, status);
        return ResponseEntity.ok("Request status updated to " + status);
    }


    @RequireRole("RECIPIENT")
    @GetMapping("/requests/{requestId}/status")
    public ResponseEntity<String> getRequestStatus(@PathVariable UUID requestId) {
        String status = recipientService.getRequestStatus(requestId);
        return ResponseEntity.ok(status);
    }

    @RequireRole("RECIPIENT")
    @PostMapping("/requests/{requestId}/cancel")
    public ResponseEntity<?> cancelRequest(@PathVariable UUID requestId, @RequestHeader("id") UUID userId, @Valid @RequestBody CancellationRequestDTO request) {
        try {
            CancellationResponseDTO response = recipientService.cancelRequest(requestId, userId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @RequireRole("RECIPIENT")
    @GetMapping("/requests/{requestId}/can-cancel")
    public ResponseEntity<Map<String, Boolean>> canCancelRequest(@PathVariable UUID requestId, @RequestHeader("id") UUID userId) {
        boolean canCancel = recipientService.canCancelRequest(requestId, userId);
        return ResponseEntity.ok(Map.of("canCancel", canCancel));
    }

    @RequireRole("RECIPIENT")
    @GetMapping("/profile-lock-info")
    public ResponseEntity<ProfileLockInfoDTO> getProfileLockInfo(@RequestHeader("id") UUID userId) {
        ProfileLockInfoDTO lockInfo = recipientService.getProfileLockInfo(userId);
        return ResponseEntity.ok(lockInfo);
    }

    @InternalOnly
    @GetMapping("/requests/internal/{requestId}")
    public ResponseEntity<ReceiveRequestDTO> getRequestByIdInternal(@PathVariable UUID requestId) {
        ReceiveRequestDTO request = recipientService.getRequestById(requestId);
        if (request == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(request);
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<NearbyRequestActivityDTO>> getNearbyRecipients(
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam("radius") double radius) {
        List<NearbyRequestActivityDTO> recipients = recipientService.getNearbyRecipients(latitude, longitude, radius);
        return ResponseEntity.ok(recipients);
    }

}
