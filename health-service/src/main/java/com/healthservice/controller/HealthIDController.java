package com.healthservice.controller;

import com.healthservice.dto.CreateHealthIDRequest;
import com.healthservice.dto.HealthIDDTO;
import com.healthservice.service.HealthIDService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/health-id")
@RequiredArgsConstructor
public class HealthIDController {

    private final HealthIDService healthIDService;

    @PostMapping
    public ResponseEntity<HealthIDDTO> createHealthID(
            @RequestHeader(value = "id", required = false) UUID requesterId,
            @RequestHeader(value = "roles", required = false) String rolesHeader,
            @RequestHeader(value = "role", required = false) String roleHeader,
            @Valid @RequestBody CreateHealthIDRequest request) {
        validateMigrantRole(rolesHeader, roleHeader);
        if (requesterId == null || !requesterId.equals(request.getUserId())) {
            throw new IllegalStateException("Migrant can only create their own Health ID");
        }
        HealthIDDTO healthID = healthIDService.createHealthID(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(healthID);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<HealthIDDTO> getHealthIDByUserId(@PathVariable UUID userId) {
        HealthIDDTO healthID = healthIDService.getHealthIDByUserId(userId);
        return ResponseEntity.ok(healthID);
    }

    @GetMapping("/{healthId}")
    public ResponseEntity<HealthIDDTO> getHealthIDByHealthId(@PathVariable String healthId) {
        HealthIDDTO healthID = healthIDService.getHealthIDByHealthId(healthId);
        return ResponseEntity.ok(healthID);
    }

    @GetMapping("/{healthId}/qr")
    public ResponseEntity<Map<String, String>> getQRCode(@PathVariable String healthId) {
        String qrCode = healthIDService.getQRCode(healthId);
        return ResponseEntity.ok(Map.of("qrCode", qrCode, "healthId", healthId));
    }

    @PutMapping("/{healthId}")
    public ResponseEntity<HealthIDDTO> updateHealthID(
            @RequestHeader(value = "id", required = false) UUID requesterId,
            @RequestHeader(value = "roles", required = false) String rolesHeader,
            @RequestHeader(value = "role", required = false) String roleHeader,
            @PathVariable String healthId,
            @Valid @RequestBody CreateHealthIDRequest request) {
        validateMigrantRole(rolesHeader, roleHeader);
        HealthIDDTO existing = healthIDService.getHealthIDByHealthId(healthId);
        if (requesterId == null || !requesterId.equals(existing.getUserId())) {
            throw new IllegalStateException("Migrant can only update their own Health ID");
        }
        if (!request.getUserId().equals(existing.getUserId())) {
            throw new IllegalStateException("Health ID update payload user mismatch");
        }
        HealthIDDTO updated = healthIDService.updateHealthID(healthId, request);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{healthId}/verify-emergency")
    public ResponseEntity<Map<String, Boolean>> verifyEmergencyAccess(
            @PathVariable String healthId,
            @RequestBody Map<String, String> request) {
        String pin = request.get("pin");
        boolean verified = healthIDService.verifyEmergencyAccess(healthId, pin);
        return ResponseEntity.ok(Map.of("verified", verified));
    }

    private void validateMigrantRole(String rolesHeader, String roleHeader) {
        String effectiveRoles = rolesHeader != null && !rolesHeader.isBlank() ? rolesHeader : roleHeader;
        if (effectiveRoles == null || !effectiveRoles.toUpperCase().contains("MIGRANT")) {
            throw new IllegalStateException("Only migrants can create or update Health ID");
        }
    }
}
