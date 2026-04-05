package com.healthservice.service;

import com.healthservice.exception.ResourceNotFoundException;
import com.healthservice.kafka.HealthEventProducer;
import com.healthservice.model.NGOMigrantAssociation;
import com.healthservice.repository.HealthIDRepository;
import com.healthservice.repository.NGOMigrantAssociationRepository;
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
public class NGOMigrantAssociationService {

    @Autowired
    private NGOMigrantAssociationRepository associationRepository;

    @Autowired
    private HealthIDRepository healthIDRepository;

    @Autowired
    private HealthEventProducer healthEventProducer;

    @Transactional
    public NGOMigrantAssociation createAssociation(UUID ngoId, String migrantHealthId, String supportType, 
                                                   String status, String description) {
        var healthID = healthIDRepository.findByHealthId(migrantHealthId)
                .orElseThrow(() -> new ResourceNotFoundException("Health ID not found: " + migrantHealthId));

        if (associationRepository.existsByNgoIdAndMigrantHealthId(ngoId, migrantHealthId)) {
            throw new IllegalStateException("Association already exists between NGO and migrant");
        }

        var newAssociation = new NGOMigrantAssociation(ngoId, migrantHealthId, healthID.getUserId(), supportType, status);
        newAssociation.setDescription(description);

        var savedAssociation = associationRepository.save(newAssociation);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("ngoId", ngoId.toString());
        eventData.put("migrantHealthId", migrantHealthId);
        eventData.put("supportType", supportType);
        eventData.put("status", status);
        healthEventProducer.publishHealthEvent("health-record-events", eventData);

        return savedAssociation;
    }

    public List<NGOMigrantAssociation> getNGOMigrants(UUID ngoId) {
        return associationRepository.findByNgoIdAndIsActiveTrue(ngoId);
    }

    public Page<NGOMigrantAssociation> getNGOMigrantsPaged(UUID ngoId, Pageable pageable) {
        return associationRepository.findByNgoIdAndIsActiveTrue(ngoId, pageable);
    }

    public List<NGOMigrantAssociation> getMigrantNGOs(String migrantHealthId) {
        return associationRepository.findByMigrantHealthIdAndIsActiveTrue(migrantHealthId);
    }

    public List<NGOMigrantAssociation> getNGOMigrantsByStatus(UUID ngoId, String status) {
        return associationRepository.findByNgoIdAndStatusAndIsActiveTrue(ngoId, status);
    }

    public List<NGOMigrantAssociation> getNGOMigrantsBySupportType(UUID ngoId, String supportType) {
        return associationRepository.findByNgoIdAndSupportTypeAndIsActiveTrue(ngoId, supportType);
    }

    public long getNGOMigrantCount(UUID ngoId) {
        return associationRepository.countActiveMigrantsForNGO(ngoId);
    }

    @Transactional
    public NGOMigrantAssociation updateAssociation(UUID associationId, String status, String notes) {
        var association = associationRepository.findById(associationId)
                .orElseThrow(() -> new ResourceNotFoundException("Association not found"));
        
        if (status != null) {
            association.setStatus(status);
        }
        if (notes != null) {
            association.setNotes(notes);
        }

        return associationRepository.save(association);
    }

    @Transactional
    public NGOMigrantAssociation endSupport(UUID associationId) {
        var association = associationRepository.findById(associationId)
                .orElseThrow(() -> new ResourceNotFoundException("Association not found"));
        
        association.setSupportEndDate(LocalDateTime.now());
        association.setStatus("COMPLETED");
        association.setActive(false);

        return associationRepository.save(association);
    }

    @Transactional
    public void deactivateAssociation(UUID associationId) {
        var association = associationRepository.findById(associationId)
                .orElseThrow(() -> new ResourceNotFoundException("Association not found"));
        association.setActive(false);
        associationRepository.save(association);
    }
}
