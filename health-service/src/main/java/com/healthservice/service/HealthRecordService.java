package com.healthservice.service;

import com.healthservice.dto.HealthRecordCommentDTO;
import com.healthservice.dto.HealthRecordCommentRequest;
import com.healthservice.dto.HealthRecordDTO;
import com.healthservice.dto.HealthRecordRequest;
import com.healthservice.exception.ResourceNotFoundException;
import com.healthservice.kafka.HealthEventProducer;
import com.healthservice.model.HealthRecord;
import com.healthservice.model.HealthRecordComment;
import com.healthservice.repository.DoctorPatientAssociationRepository;
import com.healthservice.repository.HealthRecordCommentRepository;
import com.healthservice.repository.HealthRecordRepository;
import com.healthservice.repository.NGOMigrantAssociationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HealthRecordService {

    private final HealthRecordRepository healthRecordRepository;
    private final HealthRecordCommentRepository healthRecordCommentRepository;
    private final DoctorPatientAssociationRepository doctorPatientAssociationRepository;
    private final NGOMigrantAssociationRepository ngoMigrantAssociationRepository;
    private final HealthEventProducer healthEventProducer;
    private final MlRiskTriggerService mlRiskTriggerService;

    @Transactional
    public HealthRecordDTO createHealthRecord(HealthRecordRequest request) {
        if (request.getDoctorId() == null) {
            throw new IllegalStateException("Doctor ID is required to create health records");
        }

        validateRecordTypeFields(request);
        validateDoctorAccess(request.getDoctorId(), request.getUserId());

        HealthRecord record = new HealthRecord();
        record.setUserId(request.getUserId());
        record.setHealthId(request.getHealthId());
        record.setRecordType(request.getRecordType());
        record.setTitle(request.getTitle());
        record.setDescription(request.getDescription());
        record.setDiagnosis(request.getDiagnosis());
        record.setPrescription(request.getPrescription());
        record.setTestResults(request.getTestResults());
        record.setDoctorName(request.getDoctorName());
        record.setDoctorId(request.getDoctorId());
        record.setHospitalName(request.getHospitalName());
        record.setHospitalLocation(request.getHospitalLocation());
        record.setRecordDate(request.getRecordDate());
        record.setDocumentUrl(request.getDocumentUrl());
        record.setEmergency(request.isEmergency());
        record.setNotes(request.getNotes());

        HealthRecord saved = healthRecordRepository.save(record);

        healthEventProducer.publishHealthRecordEvent(mapToDTO(saved));

        if (request.isEmergency()) {
            healthEventProducer.publishEmergencyHealthEvent(mapToDTO(saved));
            mlRiskTriggerService.triggerRiskComputation(saved.getUserId(), saved.getHealthId(), "EMERGENCY_RECORD_CREATED");
        } else {
            mlRiskTriggerService.triggerRiskComputation(saved.getUserId(), saved.getHealthId(), "HEALTH_RECORD_CREATED");
        }

        return mapToDTO(saved);
    }

    public HealthRecordDTO getHealthRecordById(UUID id) {
        HealthRecord record = healthRecordRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Health record not found"));
        return mapToDTO(record);
    }

    public List<HealthRecordDTO> getHealthRecordsByUserId(UUID userId) {
        List<HealthRecord> records = healthRecordRepository.findByUserIdOrderByRecordDateDesc(userId);
        return records.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<HealthRecordDTO> getHealthRecordsByHealthId(String healthId) {
        List<HealthRecord> records = healthRecordRepository.findByHealthIdOrderByRecordDateDesc(healthId);
        return records.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public Page<HealthRecordDTO> getHealthRecordsByUserIdPaginated(UUID userId, Pageable pageable) {
        Page<HealthRecord> records = healthRecordRepository.findByUserId(userId, pageable);
        return records.map(this::mapToDTO);
    }

    public List<HealthRecordDTO> getHealthRecordsByDateRange(UUID userId, LocalDate startDate, LocalDate endDate) {
        List<HealthRecord> records = healthRecordRepository.findByUserIdAndRecordDateBetween(userId, startDate, endDate);
        return records.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<HealthRecordDTO> getEmergencyRecords(UUID userId) {
        List<HealthRecord> records = healthRecordRepository.findByUserIdAndIsEmergencyTrue(userId);
        return records.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<HealthRecordDTO> getDoctorEmergencyRecords(UUID doctorId) {
        List<UUID> patientIds = doctorPatientAssociationRepository.findByDoctorIdAndIsActiveTrue(doctorId)
                .stream()
                .map(association -> association.getPatientUserId())
                .distinct()
                .collect(Collectors.toList());

        if (patientIds.isEmpty()) {
            return new ArrayList<>();
        }

        return healthRecordRepository.findByUserIdInAndIsEmergencyTrueOrderByRecordDateDesc(patientIds)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<HealthRecordDTO> getNGOEmergencyRecords(UUID ngoId) {
        List<UUID> migrantIds = ngoMigrantAssociationRepository.findByNgoIdAndIsActiveTrue(ngoId)
                .stream()
                .map(association -> association.getMigrantUserId())
                .distinct()
                .collect(Collectors.toList());

        if (migrantIds.isEmpty()) {
            return new ArrayList<>();
        }

        return healthRecordRepository.findByUserIdInAndIsEmergencyTrueOrderByRecordDateDesc(migrantIds)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public long getRecordCount(UUID userId) {
        return healthRecordRepository.countByUserId(userId);
    }

    @Transactional
    public HealthRecordDTO updateHealthRecord(UUID id, HealthRecordRequest request) {
        if (request.getDoctorId() == null) {
            throw new IllegalStateException("Doctor ID is required to update health records");
        }

        validateRecordTypeFields(request);
        HealthRecord record = healthRecordRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Health record not found"));

        validateDoctorAccess(request.getDoctorId(), record.getUserId());

        record.setRecordType(request.getRecordType());
        record.setTitle(request.getTitle());
        record.setDescription(request.getDescription());
        record.setDiagnosis(request.getDiagnosis());
        record.setPrescription(request.getPrescription());
        record.setTestResults(request.getTestResults());
        record.setDoctorName(request.getDoctorName());
        record.setDoctorId(request.getDoctorId());
        record.setHospitalName(request.getHospitalName());
        record.setHospitalLocation(request.getHospitalLocation());
        record.setRecordDate(request.getRecordDate());
        record.setDocumentUrl(request.getDocumentUrl());
        record.setEmergency(request.isEmergency());
        record.setNotes(request.getNotes());

        HealthRecord updated = healthRecordRepository.save(record);
        mlRiskTriggerService.triggerRiskComputation(
                updated.getUserId(),
                updated.getHealthId(),
                request.isEmergency() ? "EMERGENCY_RECORD_CREATED" : "HEALTH_RECORD_UPDATED"
        );
        return mapToDTO(updated);
    }

    @Transactional
    public void deleteHealthRecord(UUID id, UUID doctorId) {
        HealthRecord record = healthRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Health record not found"));

        validateDoctorAccess(doctorId, record.getUserId());
        healthRecordRepository.delete(record);
    }

    public List<HealthRecordCommentDTO> getHealthRecordComments(UUID recordId) {
        if (!healthRecordRepository.existsById(recordId)) {
            throw new ResourceNotFoundException("Health record not found");
        }

        return healthRecordCommentRepository.findByHealthRecordIdOrderByCreatedAtAsc(recordId)
                .stream()
                .map(this::mapCommentToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public HealthRecordCommentDTO addHealthRecordComment(UUID recordId, HealthRecordCommentRequest request) {
        if (!"MIGRANT".equalsIgnoreCase(request.getUserRole())) {
            throw new IllegalStateException("Only migrants can add comments to health records");
        }

        HealthRecord record = healthRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Health record not found"));

        if (!record.getUserId().equals(request.getUserId())) {
            throw new IllegalStateException("Migrant can comment only on their own health records");
        }

        HealthRecordComment comment = new HealthRecordComment();
        comment.setHealthRecord(record);
        comment.setUserId(request.getUserId());
        comment.setUserRole(request.getUserRole().toUpperCase());
        comment.setComment(request.getComment().trim());

        HealthRecordComment saved = healthRecordCommentRepository.save(comment);
        return mapCommentToDTO(saved);
    }

    private HealthRecordDTO mapToDTO(HealthRecord record) {
        HealthRecordDTO dto = new HealthRecordDTO();
        dto.setId(record.getId());
        dto.setUserId(record.getUserId());
        dto.setHealthId(record.getHealthId());
        dto.setRecordType(record.getRecordType());
        dto.setTitle(record.getTitle());
        dto.setDescription(record.getDescription());
        dto.setDiagnosis(record.getDiagnosis());
        dto.setPrescription(record.getPrescription());
        dto.setTestResults(record.getTestResults());
        dto.setDoctorName(record.getDoctorName());
        dto.setDoctorId(record.getDoctorId());
        dto.setHospitalName(record.getHospitalName());
        dto.setHospitalLocation(record.getHospitalLocation());
        dto.setRecordDate(record.getRecordDate());
        dto.setDocumentUrl(record.getDocumentUrl());
        dto.setEmergency(record.isEmergency());
        dto.setNotes(record.getNotes());
        dto.setCreatedAt(record.getCreatedAt());
        dto.setUpdatedAt(record.getUpdatedAt());
        return dto;
    }

    private HealthRecordCommentDTO mapCommentToDTO(HealthRecordComment comment) {
        HealthRecordCommentDTO dto = new HealthRecordCommentDTO();
        dto.setId(comment.getId());
        dto.setHealthRecordId(comment.getHealthRecord().getId());
        dto.setUserId(comment.getUserId());
        dto.setUserRole(comment.getUserRole());
        dto.setComment(comment.getComment());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        return dto;
    }

    private void validateDoctorAccess(UUID doctorId, UUID patientUserId) {
        if (!doctorPatientAssociationRepository.existsByDoctorIdAndPatientUserIdAndIsActiveTrue(doctorId, patientUserId)) {
            throw new IllegalStateException("Doctor is not associated with this migrant");
        }
    }

    private void validateRecordTypeFields(HealthRecordRequest request) {
        if (request.getRecordType() == null) {
            throw new IllegalStateException("Record type is required");
        }

        switch (request.getRecordType()) {
            case CONSULTATION -> {
                if (isBlank(request.getDescription())) {
                    throw new IllegalStateException("Consultation record requires clinical description");
                }
                if (isBlank(request.getDiagnosis())) {
                    throw new IllegalStateException("Consultation record requires diagnosis");
                }
            }
            case PRESCRIPTION -> {
                if (isBlank(request.getPrescription())) {
                    throw new IllegalStateException("Prescription record requires prescription details");
                }
            }
            case LAB_TEST -> {
                if (isBlank(request.getTestResults())) {
                    throw new IllegalStateException("Lab test record requires test results");
                }
            }
            default -> {
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
