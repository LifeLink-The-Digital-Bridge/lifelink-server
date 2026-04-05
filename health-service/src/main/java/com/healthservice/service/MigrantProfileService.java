package com.healthservice.service;

import com.healthservice.dto.MigrantProfileDTO;
import com.healthservice.exception.DuplicateResourceException;
import com.healthservice.exception.ResourceNotFoundException;
import com.healthservice.model.MigrantProfile;
import com.healthservice.repository.MigrantProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MigrantProfileService {

    private final MigrantProfileRepository migrantProfileRepository;

    @Transactional
    public MigrantProfileDTO createMigrantProfile(MigrantProfileDTO dto) {
        if (migrantProfileRepository.existsByUserId(dto.getUserId())) {
            throw new DuplicateResourceException("Migrant profile already exists for user");
        }

        MigrantProfile profile = new MigrantProfile();
        profile.setUserId(dto.getUserId());
        profile.setHealthId(dto.getHealthId());
        profile.setBloodGroup(dto.getBloodGroup());
        profile.setHeightCm(dto.getHeightCm());
        profile.setWeightKg(dto.getWeightKg());
        profile.setAllergies(dto.getAllergies());
        profile.setChronicConditions(dto.getChronicConditions());
        profile.setCurrentMedications(dto.getCurrentMedications());
        profile.setVaccinationStatus(dto.getVaccinationStatus());
        profile.setHealthRiskScore(dto.getHealthRiskScore());
        profile.setLastCheckupDate(dto.getLastCheckupDate());

        MigrantProfile saved = migrantProfileRepository.save(profile);
        return mapToDTO(saved);
    }

    public MigrantProfileDTO getMigrantProfileByUserId(UUID userId) {
        MigrantProfile profile = migrantProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Migrant profile not found"));
        return mapToDTO(profile);
    }

    public MigrantProfileDTO getMigrantProfileByHealthId(String healthId) {
        MigrantProfile profile = migrantProfileRepository.findByHealthId(healthId)
            .orElseThrow(() -> new ResourceNotFoundException("Migrant profile not found"));
        return mapToDTO(profile);
    }

    @Transactional
    public MigrantProfileDTO updateMigrantProfile(UUID userId, MigrantProfileDTO dto) {
        MigrantProfile profile = migrantProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Migrant profile not found"));

        profile.setHealthId(dto.getHealthId());
        profile.setBloodGroup(dto.getBloodGroup());
        profile.setHeightCm(dto.getHeightCm());
        profile.setWeightKg(dto.getWeightKg());
        profile.setAllergies(dto.getAllergies());
        profile.setChronicConditions(dto.getChronicConditions());
        profile.setCurrentMedications(dto.getCurrentMedications());
        profile.setVaccinationStatus(dto.getVaccinationStatus());
        profile.setHealthRiskScore(dto.getHealthRiskScore());
        profile.setLastCheckupDate(dto.getLastCheckupDate());

        MigrantProfile updated = migrantProfileRepository.save(profile);
        return mapToDTO(updated);
    }

    private MigrantProfileDTO mapToDTO(MigrantProfile profile) {
        MigrantProfileDTO dto = new MigrantProfileDTO();
        dto.setId(profile.getId());
        dto.setUserId(profile.getUserId());
        dto.setHealthId(profile.getHealthId());
        dto.setBloodGroup(profile.getBloodGroup());
        dto.setHeightCm(profile.getHeightCm());
        dto.setWeightKg(profile.getWeightKg());
        dto.setAllergies(profile.getAllergies());
        dto.setChronicConditions(profile.getChronicConditions());
        dto.setCurrentMedications(profile.getCurrentMedications());
        dto.setVaccinationStatus(profile.getVaccinationStatus());
        dto.setHealthRiskScore(profile.getHealthRiskScore());
        dto.setLastCheckupDate(profile.getLastCheckupDate());
        return dto;
    }
}
