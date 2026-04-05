package com.healthservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MlRiskTriggerService {

    @Value("${ml.risk.enabled:true}")
    private boolean enabled;

    @Value("${ml.risk.base-url:http://ml-risk-service:8002}")
    private String mlRiskBaseUrl;

    private final RestTemplate restTemplate;

    @Async
    public void triggerRiskComputation(UUID userId, String healthId, String trigger) {
        if (!enabled || userId == null) {
            return;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", userId.toString());
            payload.put("healthId", healthId);
            payload.put("trigger", trigger);
            payload.put("requestedBy", "system");
            payload.put("requestId", UUID.randomUUID().toString());

            restTemplate.postForEntity(
                    mlRiskBaseUrl + "/api/ml-risk/v1/scores/compute",
                    new HttpEntity<>(payload, headers),
                    Map.class
            );
        } catch (Exception e) {
            log.warn("ML risk trigger failed for user {}: {}", userId, e.getMessage());
        }
    }
}
