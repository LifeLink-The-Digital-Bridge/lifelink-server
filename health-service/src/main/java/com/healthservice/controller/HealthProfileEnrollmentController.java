package com.healthservice.controller;

import com.healthservice.model.HealthID;
import com.healthservice.repository.HealthIDRepository;
import com.healthservice.service.HealthProfileEnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/health-profile")
@RequiredArgsConstructor
@Slf4j
public class HealthProfileEnrollmentController {

    private final HealthProfileEnrollmentService enrollmentService;
    private final HealthIDRepository healthIDRepository;

    @PostMapping("/enroll-as-donor")
    public ResponseEntity<Map<String, Object>> enrollAsDonor(
            @RequestHeader("Authorization") String token,
            @RequestHeader("id") UUID userId,
            @RequestHeader(value = "roles", required = false) String roles,
            @RequestBody Map<String, Object> additionalData
    ) {
        if (!containsRole(roles, "MIGRANT")) {
            return ResponseEntity.status(403).body(Map.of(
                "success", false,
                "message", "Only users with MIGRANT role can enroll as donors via Health Profile"
            ));
        }
        
        log.info("Received donor enrollment request for MIGRANT user: {}", userId);
        return enrollmentService.enrollAsDonor(userId, token, roles, additionalData);
    }

    @PostMapping("/enroll-as-recipient")
    public ResponseEntity<Map<String, Object>> enrollAsRecipient(
            @RequestHeader("Authorization") String token,
            @RequestHeader("id") UUID userId,
            @RequestHeader(value = "roles", required = false) String roles,
            @RequestBody Map<String, Object> additionalData
    ) {
        if (!containsRole(roles, "MIGRANT")) {
            return ResponseEntity.status(403).body(Map.of(
                "success", false,
                "message", "Only users with MIGRANT role can enroll as recipients via Health Profile"
            ));
        }
        
        log.info("Received recipient enrollment request for MIGRANT user: {}", userId);
        return enrollmentService.enrollAsRecipient(userId, token, roles, additionalData);
    }

    @GetMapping("/check-donor-eligibility")
    public ResponseEntity<Map<String, String>> checkDonorEligibility(@RequestParam UUID userId) {
        try {
            HealthID healthID = healthIDRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Health ID not found for user"));
            
            Map<String, String> result = enrollmentService.checkDonorEligibility(healthID);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "ERROR",
                "message", e.getMessage(),
                "reasons", ""
            ));
        }
    }

    @GetMapping("/check-recipient-eligibility")
    public ResponseEntity<Map<String, String>> checkRecipientEligibility(@RequestParam UUID userId) {
        try {
            HealthID healthID = healthIDRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Health ID not found for user"));
            
            Map<String, String> result = enrollmentService.checkRecipientEligibility(healthID);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "ERROR",
                "message", e.getMessage(),
                "reasons", ""
            ));
        }
    }

    private boolean containsRole(String rolesHeader, String role) {
        if (rolesHeader == null || rolesHeader.isBlank()) {
            return false;
        }
        String[] parts = rolesHeader.split(",");
        for (String part : parts) {
            String normalized = part == null ? "" : part.trim().toUpperCase();
            if (normalized.startsWith("ROLE_")) {
                normalized = normalized.substring(5);
            }
            if (role.equalsIgnoreCase(normalized)) {
                return true;
            }
        }
        return false;
    }
}
