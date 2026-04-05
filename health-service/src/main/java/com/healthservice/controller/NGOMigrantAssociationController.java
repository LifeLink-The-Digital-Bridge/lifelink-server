package com.healthservice.controller;

import com.healthservice.model.NGOMigrantAssociation;
import com.healthservice.service.NGOMigrantAssociationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/associations/ngo-migrant")
@CrossOrigin(origins = "*")
public class NGOMigrantAssociationController {

    @Autowired
    private NGOMigrantAssociationService associationService;

    @PostMapping
    public ResponseEntity<NGOMigrantAssociation> createAssociation(@RequestBody Map<String, Object> request) {
        UUID ngoId = UUID.fromString((String) request.get("ngoId"));
        String migrantHealthId = (String) request.get("migrantHealthId");
        String supportType = (String) request.get("supportType");
        String status = (String) request.get("status");
        String description = (String) request.get("description");

        NGOMigrantAssociation association = associationService.createAssociation(
                ngoId, migrantHealthId, supportType, status, description);
        return ResponseEntity.status(HttpStatus.CREATED).body(association);
    }

    @GetMapping("/ngo/{ngoId}/migrants")
    public ResponseEntity<List<NGOMigrantAssociation>> getNGOMigrants(@PathVariable UUID ngoId) {
        List<NGOMigrantAssociation> migrants = associationService.getNGOMigrants(ngoId);
        return ResponseEntity.ok(migrants);
    }

    @GetMapping("/ngo/{ngoId}/migrants/paged")
    public ResponseEntity<Page<NGOMigrantAssociation>> getNGOMigrantsPaged(
            @PathVariable UUID ngoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<NGOMigrantAssociation> migrants = associationService.getNGOMigrantsPaged(
                ngoId, PageRequest.of(page, size));
        return ResponseEntity.ok(migrants);
    }

    @GetMapping("/ngo/{ngoId}/migrants/status/{status}")
    public ResponseEntity<List<NGOMigrantAssociation>> getNGOMigrantsByStatus(
            @PathVariable UUID ngoId,
            @PathVariable String status) {
        List<NGOMigrantAssociation> migrants = associationService.getNGOMigrantsByStatus(ngoId, status);
        return ResponseEntity.ok(migrants);
    }

    @GetMapping("/ngo/{ngoId}/migrants/support-type/{supportType}")
    public ResponseEntity<List<NGOMigrantAssociation>> getNGOMigrantsBySupportType(
            @PathVariable UUID ngoId,
            @PathVariable String supportType) {
        List<NGOMigrantAssociation> migrants = associationService.getNGOMigrantsBySupportType(ngoId, supportType);
        return ResponseEntity.ok(migrants);
    }

    @GetMapping("/migrant/{migrantHealthId}/ngos")
    public ResponseEntity<List<NGOMigrantAssociation>> getMigrantNGOs(@PathVariable String migrantHealthId) {
        List<NGOMigrantAssociation> ngos = associationService.getMigrantNGOs(migrantHealthId);
        return ResponseEntity.ok(ngos);
    }

    @GetMapping("/ngo/{ngoId}/count")
    public ResponseEntity<Map<String, Long>> getNGOMigrantCount(@PathVariable UUID ngoId) {
        long count = associationService.getNGOMigrantCount(ngoId);
        return ResponseEntity.ok(Map.of("totalMigrants", count));
    }

    @PutMapping("/{associationId}")
    public ResponseEntity<NGOMigrantAssociation> updateAssociation(
            @PathVariable UUID associationId,
            @RequestBody Map<String, String> request) {
        String status = request.get("status");
        String notes = request.get("notes");
        NGOMigrantAssociation updated = associationService.updateAssociation(associationId, status, notes);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{associationId}/end-support")
    public ResponseEntity<NGOMigrantAssociation> endSupport(@PathVariable UUID associationId) {
        NGOMigrantAssociation updated = associationService.endSupport(associationId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{associationId}")
    public ResponseEntity<Void> deactivateAssociation(@PathVariable UUID associationId) {
        associationService.deactivateAssociation(associationId);
        return ResponseEntity.noContent().build();
    }
}
