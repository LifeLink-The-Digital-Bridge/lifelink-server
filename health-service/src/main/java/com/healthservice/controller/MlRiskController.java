package com.healthservice.controller;

import com.healthservice.dto.HealthIDDTO;
import com.healthservice.dto.HealthRecordDTO;
import com.healthservice.dto.MigrantRiskScoreCallbackRequest;
import com.healthservice.dto.MigrantRiskScoreDTO;
import com.healthservice.service.HealthIDService;
import com.healthservice.service.HealthRecordService;
import com.healthservice.service.MigrantRiskScoreService;
import com.healthservice.service.MlRiskTriggerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/health")
public class MlRiskController {

    private final MigrantRiskScoreService migrantRiskScoreService;
    private final HealthIDService healthIDService;
    private final HealthRecordService healthRecordService;
    private final MlRiskTriggerService mlRiskTriggerService;

    @Value("${internal.access-token:}")
    private String internalToken;

    @PostMapping("/risk/callback")
    public ResponseEntity<Map<String, Object>> saveRiskCallback(
            @RequestHeader(value = "x-internal-token", required = false) String requestToken,
            @Valid @RequestBody MigrantRiskScoreCallbackRequest request
    ) {
        validateInternalToken(requestToken);
        MigrantRiskScoreDTO saved = migrantRiskScoreService.saveCallbackScore(request);
        return ResponseEntity.ok(Map.of("success", true, "id", saved.getId()));
    }

    @GetMapping("/risk/{userId}/latest")
    public ResponseEntity<MigrantRiskScoreDTO> getLatestRiskByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(migrantRiskScoreService.getLatestByUserId(userId));
    }

    @PostMapping("/risk/{userId}/compute")
    public ResponseEntity<Map<String, Object>> triggerManualCompute(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "MANUAL") String trigger
    ) {
        String healthId = null;
        try {
            HealthIDDTO healthIDDTO = healthIDService.getHealthIDByUserId(userId);
            healthId = healthIDDTO.getHealthId();
        } catch (Exception ignored) {
        }
        mlRiskTriggerService.triggerRiskComputation(userId, healthId, trigger);
        return ResponseEntity.accepted().body(Map.of("success", true, "status", "QUEUED"));
    }

    @GetMapping("/internal/risk-data/{userId}")
    public ResponseEntity<Map<String, Object>> getRiskDataInput(
            @PathVariable UUID userId,
            @RequestHeader(value = "x-internal-token", required = false) String requestToken
    ) {
        validateInternalToken(requestToken);
        HealthIDDTO healthID = healthIDService.getHealthIDByUserId(userId);
        List<HealthRecordDTO> records = healthRecordService.getHealthRecordsByUserId(userId);

        List<HealthRecordDTO> limited = records.size() > 30 ? records.subList(0, 30) : records;

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId.toString());
        response.put("healthId", healthID);
        response.put("recentRecords", limited);
        response.put("recordCount", records.size());
        return ResponseEntity.ok(response);
    }

    private void validateInternalToken(String token) {
        if (internalToken != null && !internalToken.isBlank()) {
            if (token == null || !internalToken.equals(token)) {
                throw new IllegalStateException("Unauthorized internal access");
            }
        }
    }
}
