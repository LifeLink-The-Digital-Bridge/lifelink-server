package com.healthservice.service;

import feign.FeignException;
import com.healthservice.feign.DonorServiceClient;
import com.healthservice.feign.RecipientServiceClient;
import com.healthservice.model.HealthID;
import com.healthservice.repository.HealthIDRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthProfileEnrollmentService {

    private final HealthIDRepository healthIDRepository;
    private final DonorServiceClient donorServiceClient;
    private final RecipientServiceClient recipientServiceClient;

    @Transactional
    public ResponseEntity<Map<String, Object>> enrollAsDonor(UUID userId, String token, String rolesHeader, Map<String, Object> additionalData) {
        HealthID healthID = healthIDRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Health ID not found for user: " + userId));

        validateConsent(additionalData, "donor");

        Map<String, String> eligibilityCheck = checkDonorEligibility(healthID);
        if (!"ELIGIBLE".equals(eligibilityCheck.get("status"))) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", eligibilityCheck.get("message"));
            errorResponse.put("reasons", eligibilityCheck.get("reasons"));
            return ResponseEntity.badRequest().body(errorResponse);
        }

        Map<String, Object> registerDonorPayload = mapHealthIDToDonorPayload(healthID, additionalData);

        try {
            ResponseEntity<Map<String, Object>> donorResponse = donorServiceClient.createDonorProfile(
                    token,
                    userId.toString(),
                    rolesHeader,
                    registerDonorPayload
            );
            
            log.info("Successfully enrolled user {} as donor via health profile", userId);
            return donorResponse;
        } catch (FeignException e) {
            int status = e.status() > 0 ? e.status() : 500;
            log.error("Donor enrollment downstream error for user {}: status={}, body={}", userId, status, e.contentUTF8());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Donor enrollment failed");
            errorResponse.put("downstreamStatus", status);
            errorResponse.put("downstreamError", e.contentUTF8());
            return ResponseEntity.status(status).body(errorResponse);
        } catch (Exception e) {
            log.error("Failed to enroll user {} as donor: {}", userId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Donor enrollment failed: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> enrollAsRecipient(UUID userId, String token, String rolesHeader, Map<String, Object> additionalData) {
        HealthID healthID = healthIDRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Health ID not found for user: " + userId));

        validateConsent(additionalData, "recipient");

        Map<String, String> eligibilityCheck = checkRecipientEligibility(healthID);
        if (!"ELIGIBLE".equals(eligibilityCheck.get("status"))) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", eligibilityCheck.get("message"));
            return ResponseEntity.badRequest().body(errorResponse);
        }

        Map<String, Object> registerRecipientPayload = mapHealthIDToRecipientPayload(healthID, additionalData);

        try {
            ResponseEntity<Map<String, Object>> recipientResponse = recipientServiceClient.createRecipientProfile(
                    token,
                    userId.toString(),
                    rolesHeader,
                    registerRecipientPayload
            );
            
            log.info("Successfully enrolled user {} as recipient via health profile", userId);
            return recipientResponse;
        } catch (FeignException e) {
            int status = e.status() > 0 ? e.status() : 500;
            log.error("Recipient enrollment downstream error for user {}: status={}, body={}", userId, status, e.contentUTF8());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Recipient enrollment failed");
            errorResponse.put("downstreamStatus", status);
            errorResponse.put("downstreamError", e.contentUTF8());
            return ResponseEntity.status(status).body(errorResponse);
        } catch (Exception e) {
            log.error("Failed to enroll user {} as recipient: {}", userId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Recipient enrollment failed: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    public Map<String, String> checkDonorEligibility(HealthID healthID) {
        Map<String, String> result = new HashMap<>();
        StringBuilder reasons = new StringBuilder();
        
        if (healthID.getBloodGroup() == null || healthID.getBloodGroup().isEmpty()) {
            reasons.append("Blood group is required. ");
        }
        if (healthID.getRhFactor() == null || healthID.getRhFactor().isEmpty()) {
            reasons.append("Rh factor is required. ");
        }
        if (healthID.getHemoglobinLevel() == null) {
            reasons.append("Hemoglobin level is required. ");
        } else if (healthID.getHemoglobinLevel() < 12.5) {
            reasons.append("Hemoglobin level must be at least 12.5 g/dL. ");
        }
        if (Boolean.TRUE.equals(healthID.getHasDiabetes())) {
            reasons.append("Donors with diabetes are not eligible. ");
        }
        if (Boolean.TRUE.equals(healthID.getHasChronicDiseases())) {
            reasons.append("Donors with chronic diseases require medical clearance. ");
        }
        
        if (reasons.length() > 0) {
            result.put("status", "INELIGIBLE");
            result.put("message", "Donor eligibility requirements not met");
            result.put("reasons", reasons.toString().trim());
        } else {
            result.put("status", "ELIGIBLE");
            result.put("message", "User is eligible to enroll as donor");
            result.put("reasons", "");
        }
        
        return result;
    }

    public Map<String, String> checkRecipientEligibility(HealthID healthID) {
        Map<String, String> result = new HashMap<>();
        StringBuilder reasons = new StringBuilder();
        
        if (healthID.getBloodGroup() == null || healthID.getBloodGroup().isEmpty()) {
            reasons.append("Blood group is required. ");
        }
        if (healthID.getRhFactor() == null || healthID.getRhFactor().isEmpty()) {
            reasons.append("Rh factor is required. ");
        }
        if (healthID.getMedicalHistory() == null || healthID.getMedicalHistory().isEmpty()) {
            reasons.append("Medical history is required. ");
        }
        
        if (reasons.length() > 0) {
            result.put("status", "INELIGIBLE");
            result.put("message", "Recipient enrollment requirements not met");
            result.put("reasons", reasons.toString().trim());
        } else {
            result.put("status", "ELIGIBLE");
            result.put("message", "User is eligible to enroll as recipient");
            result.put("reasons", "");
        }
        
        return result;
    }

    private Map<String, Object> mapHealthIDToDonorPayload(HealthID healthID, Map<String, Object> additionalData) {
        Map<String, Object> payload = new HashMap<>();
        
        payload.put("registrationDate", LocalDate.now());
        payload.put("status", "ACTIVE");
        
        Map<String, Object> medicalDetails = new HashMap<>();
        medicalDetails.put("hemoglobinLevel", healthID.getHemoglobinLevel());
        medicalDetails.put("bloodGlucoseLevel", additionalData.getOrDefault("bloodGlucoseLevel", 90.0));
        medicalDetails.put("hasDiabetes", healthID.getHasDiabetes() != null ? healthID.getHasDiabetes() : false);
        medicalDetails.put("bloodPressure", healthID.getBloodPressure() != null ? healthID.getBloodPressure() : "120/80");
        medicalDetails.put("hasDiseases", healthID.getHasChronicDiseases() != null ? healthID.getHasChronicDiseases() : false);
        medicalDetails.put("takingMedication", healthID.getCurrentMedications() != null && !healthID.getCurrentMedications().isEmpty());
        medicalDetails.put("diseaseDescription", healthID.getChronicConditions());
        medicalDetails.put("currentMedications", healthID.getCurrentMedications());
        medicalDetails.put("lastMedicalCheckup", healthID.getLastCheckupDate() != null ? healthID.getLastCheckupDate().toLocalDate() : LocalDate.now());
        medicalDetails.put("medicalHistory", healthID.getMedicalHistory());
        medicalDetails.put("hasInfectiousDiseases", false);
        medicalDetails.put("infectiousDiseaseDetails", "");
        medicalDetails.put("creatinineLevel", additionalData.getOrDefault("creatinineLevel", 1.0));
        medicalDetails.put("liverFunctionTests", additionalData.getOrDefault("liverFunctionTests", "Normal"));
        medicalDetails.put("cardiacStatus", additionalData.getOrDefault("cardiacStatus", "Normal"));
        medicalDetails.put("pulmonaryFunction", additionalData.getOrDefault("pulmonaryFunction", 95.0));
        medicalDetails.put("overallHealthStatus", additionalData.getOrDefault("overallHealthStatus", "Good"));
        payload.put("medicalDetails", medicalDetails);
        
        Map<String, Object> eligibility = new HashMap<>();
        int defaultAge = (int) additionalData.getOrDefault("age", 30);
        double defaultBmi = 22.0;
        if (healthID.getHeightCm() != null && healthID.getWeightKg() != null && healthID.getHeightCm() > 0) {
            double heightInMeters = healthID.getHeightCm() / 100.0;
            defaultBmi = healthID.getWeightKg() / (heightInMeters * heightInMeters);
        }
        eligibility.put("ageEligible", true);
        eligibility.put("age", defaultAge);
        eligibility.put("dob", additionalData.getOrDefault("dob", LocalDate.now().minusYears(defaultAge)));
        eligibility.put("weightEligible", healthID.getWeightKg() != null && healthID.getWeightKg() >= 50);
        eligibility.put("weight", healthID.getWeightKg());
        eligibility.put("medicalClearance", true);
        eligibility.put("recentTattooOrPiercing", false);
        eligibility.put("recentTravelDetails", "");
        eligibility.put("recentVaccination", false);
        eligibility.put("recentSurgery", false);
        eligibility.put("chronicDiseases", healthID.getChronicConditions());
        eligibility.put("allergies", healthID.getAllergies());
        eligibility.put("lastDonationDate", null);
        eligibility.put("height", healthID.getHeightCm());
        eligibility.put("bodyMassIndex", additionalData.getOrDefault("bodyMassIndex", defaultBmi));
        eligibility.put("bodySize", additionalData.get("bodySize"));
        eligibility.put("isLivingDonor", false);
        payload.put("eligibilityCriteria", eligibility);
        
        Map<String, Object> consent = new HashMap<>();
        consent.put("isConsented", additionalData.getOrDefault("consentGiven", false));
        consent.put("consentedAt", additionalData.getOrDefault("consentedAt", LocalDateTime.now()));
        consent.put("consentType", additionalData.getOrDefault("consentType", "GENERAL"));
        payload.put("consentForm", consent);
        
        payload.put("addresses", additionalData.getOrDefault("addresses", new ArrayList<>()));
        payload.put("hlaProfile", additionalData.getOrDefault("hlaProfile", null));
        
        return payload;
    }

    private Map<String, Object> mapHealthIDToRecipientPayload(HealthID healthID, Map<String, Object> additionalData) {
        Map<String, Object> payload = new HashMap<>();
        
        payload.put("availability", "AVAILABLE");

        Map<String, Object> medicalDetails = new HashMap<>();
        medicalDetails.put("hemoglobinLevel", healthID.getHemoglobinLevel());
        medicalDetails.put("bloodGlucoseLevel", additionalData.getOrDefault("bloodGlucoseLevel", 90.0));
        medicalDetails.put("hasDiabetes", healthID.getHasDiabetes() != null ? healthID.getHasDiabetes() : false);
        medicalDetails.put("bloodPressure", healthID.getBloodPressure() != null ? healthID.getBloodPressure() : "120/80");
        medicalDetails.put("diagnosis", additionalData.getOrDefault("diagnosis", "To be evaluated"));
        medicalDetails.put("allergies", healthID.getAllergies());
        medicalDetails.put("currentMedications", healthID.getCurrentMedications());
        medicalDetails.put("additionalNotes", healthID.getMedicalHistory());
        medicalDetails.put("hasInfectiousDiseases", false);
        medicalDetails.put("infectiousDiseaseDetails", "");
        medicalDetails.put("creatinineLevel", additionalData.getOrDefault("creatinineLevel", 1.0));
        medicalDetails.put("liverFunctionTests", additionalData.getOrDefault("liverFunctionTests", "Normal"));
        medicalDetails.put("cardiacStatus", additionalData.getOrDefault("cardiacStatus", "Normal"));
        medicalDetails.put("pulmonaryFunction", additionalData.getOrDefault("pulmonaryFunction", 95.0));
        medicalDetails.put("overallHealthStatus", additionalData.getOrDefault("overallHealthStatus", "Stable"));
        payload.put("medicalDetails", medicalDetails);

        Map<String, Object> eligibility = new HashMap<>();
        eligibility.put("ageEligible", true);
        eligibility.put("age", additionalData.getOrDefault("age", 30));
        eligibility.put("dob", additionalData.get("dob"));
        eligibility.put("weightEligible", healthID.getWeightKg() != null && healthID.getWeightKg() >= 40);
        eligibility.put("weight", healthID.getWeightKg());
        eligibility.put("medicallyEligible", true);
        eligibility.put("legalClearance", true);
        eligibility.put("notes", additionalData.getOrDefault("urgency", "MEDIUM"));
        eligibility.put("lastReviewed", null);
        eligibility.put("height", healthID.getHeightCm());
        eligibility.put("bodyMassIndex", additionalData.get("bodyMassIndex"));
        eligibility.put("bodySize", additionalData.get("bodySize"));
        eligibility.put("isLivingDonor", false);
        payload.put("eligibilityCriteria", eligibility);

        Map<String, Object> consent = new HashMap<>();
        consent.put("isConsented", additionalData.getOrDefault("consentGiven", false));
        consent.put("consentedAt", additionalData.getOrDefault("consentedAt", LocalDateTime.now()));
        payload.put("consentForm", consent);

        payload.put("addresses", additionalData.getOrDefault("addresses", new ArrayList<>()));
        payload.put("hlaProfile", additionalData.getOrDefault("hlaProfile", null));
        
        return payload;
    }

    private void validateConsent(Map<String, Object> additionalData, String enrollmentType) {
        Object consentGiven = additionalData.get("consentGiven");
        if (!(consentGiven instanceof Boolean) || !((Boolean) consentGiven)) {
            throw new RuntimeException("Explicit consent is required for " + enrollmentType + " enrollment");
        }
    }
}
