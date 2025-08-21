package com.recipientservice.controller;

import com.recipientservice.aop.RequireRole;
import com.recipientservice.client.UserClient;
import com.recipientservice.dto.*;
import com.recipientservice.service.RecipientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("/register")
    public ResponseEntity<RecipientDTO> registerRecipient(@RequestHeader("id") UUID userId, @RequestBody RegisterRecipientDTO recipientDTO) {
        RecipientDTO recipient = recipientService.createRecipient(userId, recipientDTO);
        return ResponseEntity.ok(recipient);
    }

    @RequireRole("RECIPIENT")
    @PostMapping("/request")
    public ResponseEntity<ReceiveRequestDTO> createReceiveRequest(@RequestHeader("id") UUID userId, @RequestBody CreateReceiveRequestDTO  requestDTO) {
        ReceiveRequestDTO response = recipientService.createReceiveRequest(userId, requestDTO);
        return ResponseEntity.ok(response);
    }

    @RequireRole("RECIPIENT")
    @GetMapping("/by-userId")
    public ResponseEntity<RecipientDTO> getRecipientByUserId(@RequestHeader("id") UUID userId) {
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
}
