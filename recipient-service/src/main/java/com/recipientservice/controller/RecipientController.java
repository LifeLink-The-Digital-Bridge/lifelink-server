package com.recipientservice.controller;

import com.recipientservice.aop.InternalOnly;
import com.recipientservice.aop.RequireRole;
import com.recipientservice.client.UserClient;
import com.recipientservice.dto.*;
import com.recipientservice.dto.CreateRecipientHistoryRequest;
import com.recipientservice.enums.RequestStatus;
import com.recipientservice.exceptions.AccessDeniedException;
import com.recipientservice.service.RecipientService;
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

    public RecipientController(UserClient userClient, RecipientService recipientService) {
        this.userClient = userClient;
        this.recipientService = recipientService;
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
    public ResponseEntity<ReceiveRequestDTO> createReceiveRequest(@RequestHeader("id") UUID userId, @RequestBody CreateReceiveRequestDTO  requestDTO) {
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

    @RequireRole("RECIPIENT")
    @GetMapping("/{recipientId}/requests")
    public ResponseEntity<List<ReceiveRequestDTO>> getReceiveRequests(@PathVariable UUID recipientId) {
        return ResponseEntity.ok(recipientService.getReceiveRequestsByRecipientId(recipientId));
    }

    @RequireRole("RECIPIENT")
    @GetMapping("/my-requests")
    public ResponseEntity<List<ReceiveRequestDTO>> getMyRequests(@RequestHeader("id") UUID userId) {
        return ResponseEntity.ok(recipientService.getReceiveRequestsByUserId(userId));
    }

    @InternalOnly
    @PutMapping("/requests/{requestId}/status/fulfilled")
    public ResponseEntity<String> updateRequestStatusToFulfilled(@PathVariable UUID requestId) {
        recipientService.updateRequestStatus(requestId, RequestStatus.FULFILLED);
        return ResponseEntity.ok("Request status updated to fulfilled");
    }

    @RequireRole("RECIPIENT")
    @GetMapping("/requests/{requestId}/status")
    public ResponseEntity<String> getRequestStatus(@PathVariable UUID requestId) {
        String status = recipientService.getRequestStatus(requestId);
        return ResponseEntity.ok(status);
    }

    @InternalOnly
    @PostMapping("/history/create")
    public ResponseEntity<String> createRecipientHistory(@RequestBody CreateRecipientHistoryRequest request) {
        recipientService.createRecipientHistory(request);
        return ResponseEntity.ok("Recipient history created");
    }
    
    @GetMapping("/user/{userId}/history")
    public ResponseEntity<List<RecipientHistoryDTO>> getRecipientHistory(@PathVariable UUID userId) {
        List<RecipientHistoryDTO> history = recipientService.getRecipientHistory(userId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/history/match/{matchId}")
    public ResponseEntity<List<RecipientHistoryDTO>> getRecipientHistoryByMatch(
            @PathVariable UUID matchId,
            @RequestHeader("id") UUID requestingUserId) {

        try {
            List<RecipientHistoryDTO> history = recipientService.getRecipientHistoryByMatchId(matchId, requestingUserId);
            return ResponseEntity.ok(history);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).build();
        }
    }

    @GetMapping("/history/for-donor/{recipientUserId}")
    public ResponseEntity<List<RecipientHistoryDTO>> getRecipientHistoryForDonor(
            @PathVariable UUID recipientUserId,
            @RequestHeader("id") UUID donorUserId) {

        try {
            List<RecipientHistoryDTO> history = recipientService.getRecipientHistoryForDonor(recipientUserId, donorUserId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(403).build();
        }
    }


}
