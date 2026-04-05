package com.healthservice.service;

import com.healthservice.exception.ResourceNotFoundException;
import com.healthservice.kafka.HealthEventProducer;
import com.healthservice.model.DoctorPatientAssociation;
import com.healthservice.repository.DoctorPatientAssociationRepository;
import com.healthservice.repository.HealthIDRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DoctorPatientAssociationService {

    @Autowired
    private DoctorPatientAssociationRepository associationRepository;

    @Autowired
    private HealthIDRepository healthIDRepository;

    @Autowired
    private HealthEventProducer healthEventProducer;

    @Transactional
    public DoctorPatientAssociation createOrUpdateAssociation(UUID doctorId, String patientHealthId, String notes) {
        var healthID = healthIDRepository.findByHealthId(patientHealthId)
                .orElseThrow(() -> new ResourceNotFoundException("Health ID not found: " + patientHealthId));

        var existingAssociation = associationRepository.findByDoctorIdAndPatientHealthId(doctorId, patientHealthId);

        if (existingAssociation.isPresent()) {
            var association = existingAssociation.get();
            association.setLastConsultationDate(LocalDateTime.now());
            association.setTotalConsultations(association.getTotalConsultations() + 1);
            if (notes != null) {
                association.setNotes(notes);
            }
            return associationRepository.save(association);
        } else {
            var newAssociation = new DoctorPatientAssociation(doctorId, patientHealthId, healthID.getUserId());
            if (notes != null) {
                newAssociation.setNotes(notes);
            }

            var savedAssociation = associationRepository.save(newAssociation);

            Map<String, Object> eventData = new HashMap<>();
            eventData.put("doctorId", doctorId.toString());
            eventData.put("patientHealthId", patientHealthId);
            eventData.put("associationType", "DOCTOR_PATIENT_LINK");
            healthEventProducer.publishHealthEvent("health-record-events", eventData);

            return savedAssociation;
        }
    }

    public List<DoctorPatientAssociation> getDoctorPatients(UUID doctorId) {
        return associationRepository.findByDoctorIdAndIsActiveTrue(doctorId);
    }

    public Page<DoctorPatientAssociation> getDoctorPatientsPaged(UUID doctorId, Pageable pageable) {
        return associationRepository.findByDoctorIdAndIsActiveTrue(doctorId, pageable);
    }

    public List<DoctorPatientAssociation> getPatientDoctors(String patientHealthId) {
        return associationRepository.findByPatientHealthIdAndIsActiveTrue(patientHealthId);
    }

    public long getDoctorPatientCount(UUID doctorId) {
        return associationRepository.countActivePatientsForDoctor(doctorId);
    }

    @Transactional
    public void deactivateAssociation(UUID associationId) {
        var association = associationRepository.findById(associationId)
                .orElseThrow(() -> new ResourceNotFoundException("Association not found"));
        association.setActive(false);
        associationRepository.save(association);
    }

    @Transactional
    public DoctorPatientAssociation updateNotes(UUID associationId, String notes) {
        var association = associationRepository.findById(associationId)
                .orElseThrow(() -> new ResourceNotFoundException("Association not found"));
        association.setNotes(notes);
        return associationRepository.save(association);
    }
}
