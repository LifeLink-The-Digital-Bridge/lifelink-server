package com.healthservice.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.healthservice.dto.CreateHealthIDRequest;
import com.healthservice.dto.HealthIDDTO;
import com.healthservice.exception.DuplicateResourceException;
import com.healthservice.exception.ResourceNotFoundException;
import com.healthservice.model.HealthID;
import com.healthservice.repository.HealthIDRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HealthIDService {

    private final HealthIDRepository healthIDRepository;
    private final MlRiskTriggerService mlRiskTriggerService;

    @Transactional
    public HealthIDDTO createHealthID(CreateHealthIDRequest request) {
        if (healthIDRepository.existsByUserId(request.getUserId())) {
            throw new DuplicateResourceException("Health ID already exists for user");
        }

        String healthId = generateHealthID();
        
        HealthID healthIDEntity = new HealthID();
        healthIDEntity.setUserId(request.getUserId());
        healthIDEntity.setHealthId(healthId);
        healthIDEntity.setBloodGroup(request.getBloodGroup());
        healthIDEntity.setRhFactor(request.getRhFactor());
        healthIDEntity.setAllergies(request.getAllergies());
        healthIDEntity.setChronicConditions(request.getChronicConditions());
        healthIDEntity.setEmergencyContactName(request.getEmergencyContactName());
        healthIDEntity.setEmergencyContactPhone(request.getEmergencyContactPhone());
        healthIDEntity.setEmergencyPin(request.getEmergencyPin());
        healthIDEntity.setHeightCm(request.getHeightCm());
        healthIDEntity.setWeightKg(request.getWeightKg());
        healthIDEntity.setCurrentMedications(request.getCurrentMedications());
        healthIDEntity.setVaccinationStatus(request.getVaccinationStatus());
        healthIDEntity.setMedicalHistory(request.getMedicalHistory());
        healthIDEntity.setHasChronicDiseases(request.getHasChronicDiseases());
        healthIDEntity.setHasDiabetes(request.getHasDiabetes());
        healthIDEntity.setBloodPressure(request.getBloodPressure());
        healthIDEntity.setHemoglobinLevel(request.getHemoglobinLevel());
        healthIDEntity.setLastCheckupDate(request.getLastCheckupDate());
        healthIDEntity.setCurrentCity(request.getCurrentCity());
        healthIDEntity.setCurrentState(request.getCurrentState());
        healthIDEntity.setOccupation(request.getOccupation());
        healthIDEntity.setPreferredLanguage(request.getPreferredLanguage());
        
        String qrCodeData = generateQRCode(healthId);
        healthIDEntity.setQrCodeData(qrCodeData);
        
        HealthID saved = healthIDRepository.save(healthIDEntity);
        mlRiskTriggerService.triggerRiskComputation(saved.getUserId(), saved.getHealthId(), "HEALTH_ID_CREATED");
        
        return mapToDTO(saved);
    }

    public HealthIDDTO getHealthIDByUserId(UUID userId) {
        HealthID healthID = healthIDRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Health ID not found for user"));
        return mapToDTO(healthID);
    }

    public HealthIDDTO getHealthIDByHealthId(String healthId) {
        HealthID healthID = healthIDRepository.findByHealthId(healthId)
            .orElseThrow(() -> new ResourceNotFoundException("Health ID not found"));
        return mapToDTO(healthID);
    }

    public String getQRCode(String healthId) {
        HealthID healthID = healthIDRepository.findByHealthId(healthId)
            .orElseThrow(() -> new ResourceNotFoundException("Health ID not found"));
        return healthID.getQrCodeData();
    }

    @Transactional
    public HealthIDDTO updateHealthID(String healthId, CreateHealthIDRequest request) {
        HealthID healthID = healthIDRepository.findByHealthId(healthId)
            .orElseThrow(() -> new ResourceNotFoundException("Health ID not found"));
        
        healthID.setBloodGroup(request.getBloodGroup());
        healthID.setRhFactor(request.getRhFactor());
        healthID.setAllergies(request.getAllergies());
        healthID.setChronicConditions(request.getChronicConditions());
        healthID.setEmergencyContactName(request.getEmergencyContactName());
        healthID.setEmergencyContactPhone(request.getEmergencyContactPhone());
        healthID.setHeightCm(request.getHeightCm());
        healthID.setWeightKg(request.getWeightKg());
        healthID.setCurrentMedications(request.getCurrentMedications());
        healthID.setVaccinationStatus(request.getVaccinationStatus());
        healthID.setMedicalHistory(request.getMedicalHistory());
        healthID.setHasChronicDiseases(request.getHasChronicDiseases());
        healthID.setHasDiabetes(request.getHasDiabetes());
        healthID.setBloodPressure(request.getBloodPressure());
        healthID.setHemoglobinLevel(request.getHemoglobinLevel());
        healthID.setLastCheckupDate(request.getLastCheckupDate());
        healthID.setCurrentCity(request.getCurrentCity());
        healthID.setCurrentState(request.getCurrentState());
        healthID.setOccupation(request.getOccupation());
        healthID.setPreferredLanguage(request.getPreferredLanguage());
        
        if (request.getEmergencyPin() != null && !request.getEmergencyPin().isEmpty()) {
            healthID.setEmergencyPin(request.getEmergencyPin());
        }
        
        HealthID updated = healthIDRepository.save(healthID);
        mlRiskTriggerService.triggerRiskComputation(updated.getUserId(), updated.getHealthId(), "HEALTH_ID_UPDATED");
        return mapToDTO(updated);
    }

    public boolean verifyEmergencyAccess(String healthId, String pin) {
        HealthID healthID = healthIDRepository.findByHealthId(healthId)
            .orElseThrow(() -> new ResourceNotFoundException("Health ID not found"));
        return healthID.getEmergencyPin().equals(pin);
    }

    private String generateHealthID() {
        return "HEALTH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateQRCode(String healthId) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            
            BitMatrix bitMatrix = qrCodeWriter.encode(healthId, BarcodeFormat.QR_CODE, 300, 300, hints);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            
            byte[] qrCodeBytes = outputStream.toByteArray();
            return Base64.getEncoder().encodeToString(qrCodeBytes);
            
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    private HealthIDDTO mapToDTO(HealthID healthID) {
        HealthIDDTO dto = new HealthIDDTO();
        dto.setId(healthID.getId());
        dto.setUserId(healthID.getUserId());
        dto.setHealthId(healthID.getHealthId());
        dto.setQrCodeData(healthID.getQrCodeData());
        dto.setBloodGroup(healthID.getBloodGroup());
        dto.setRhFactor(healthID.getRhFactor());
        dto.setAllergies(healthID.getAllergies());
        dto.setChronicConditions(healthID.getChronicConditions());
        dto.setEmergencyContactName(healthID.getEmergencyContactName());
        dto.setEmergencyContactPhone(healthID.getEmergencyContactPhone());
        dto.setHeightCm(healthID.getHeightCm());
        dto.setWeightKg(healthID.getWeightKg());
        dto.setCurrentMedications(healthID.getCurrentMedications());
        dto.setVaccinationStatus(healthID.getVaccinationStatus());
        dto.setMedicalHistory(healthID.getMedicalHistory());
        dto.setHasChronicDiseases(healthID.getHasChronicDiseases());
        dto.setHasDiabetes(healthID.getHasDiabetes());
        dto.setBloodPressure(healthID.getBloodPressure());
        dto.setHemoglobinLevel(healthID.getHemoglobinLevel());
        dto.setLastCheckupDate(healthID.getLastCheckupDate());
        dto.setCurrentCity(healthID.getCurrentCity());
        dto.setCurrentState(healthID.getCurrentState());
        dto.setOccupation(healthID.getOccupation());
        dto.setPreferredLanguage(healthID.getPreferredLanguage());
        dto.setActive(healthID.isActive());
        dto.setCreatedAt(healthID.getCreatedAt());
        dto.setUpdatedAt(healthID.getUpdatedAt());
        return dto;
    }
}
