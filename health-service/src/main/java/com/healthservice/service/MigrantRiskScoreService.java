package com.healthservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthservice.dto.MigrantRiskScoreCallbackRequest;
import com.healthservice.dto.MigrantRiskScoreDTO;
import com.healthservice.exception.ResourceNotFoundException;
import com.healthservice.model.MigrantRiskScore;
import com.healthservice.repository.MigrantRiskScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MigrantRiskScoreService {

    private final MigrantRiskScoreRepository migrantRiskScoreRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public MigrantRiskScoreDTO saveCallbackScore(MigrantRiskScoreCallbackRequest request) {
        MigrantRiskScore entity = migrantRiskScoreRepository.findByUserId(request.getUserId())
                .orElseGet(MigrantRiskScore::new);

        entity.setUserId(request.getUserId());
        entity.setHealthId(request.getHealthId());
        entity.setRiskScore(request.getRiskScore());
        entity.setRiskLevel(request.getRiskLevel());
        entity.setTopFactorsJson(toJson(request.getTopFactors()));
        entity.setRecommendedActionsJson(toJson(request.getRecommendedActions()));
        entity.setModelVersion(request.getModelVersion());
        entity.setComputedAt(request.getComputedAt() != null ? request.getComputedAt() : LocalDateTime.now());

        MigrantRiskScore saved = migrantRiskScoreRepository.save(entity);
        return mapToDTO(saved);
    }

    public MigrantRiskScoreDTO getLatestByUserId(UUID userId) {
        MigrantRiskScore entity = migrantRiskScoreRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Risk score not found for user"));
        return mapToDTO(entity);
    }

    private MigrantRiskScoreDTO mapToDTO(MigrantRiskScore entity) {
        MigrantRiskScoreDTO dto = new MigrantRiskScoreDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setHealthId(entity.getHealthId());
        dto.setRiskScore(entity.getRiskScore());
        dto.setRiskLevel(entity.getRiskLevel());
        dto.setTopFactors(fromJson(entity.getTopFactorsJson()));
        dto.setRecommendedActions(fromJson(entity.getRecommendedActionsJson()));
        dto.setModelVersion(entity.getModelVersion());
        dto.setComputedAt(entity.getComputedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    private String toJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? Collections.emptyList() : values);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize risk details", e);
        }
    }

    private List<String> fromJson(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
