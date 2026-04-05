package com.healthservice.controller;

import com.healthservice.model.DoctorPatientAssociation;
import com.healthservice.service.DoctorPatientAssociationService;
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
@RequestMapping("/api/associations/doctor-patient")
@CrossOrigin(origins = "*")
public class DoctorPatientAssociationController {

    @Autowired
    private DoctorPatientAssociationService associationService;

    @PostMapping
    public ResponseEntity<DoctorPatientAssociation> createAssociation(@RequestBody Map<String, Object> request) {
        UUID doctorId = UUID.fromString((String) request.get("doctorId"));
        String patientHealthId = (String) request.get("patientHealthId");
        String notes = (String) request.get("notes");

        DoctorPatientAssociation association = associationService.createOrUpdateAssociation(doctorId, patientHealthId, notes);
        return ResponseEntity.status(HttpStatus.CREATED).body(association);
    }

    @GetMapping("/doctor/{doctorId}/patients")
    public ResponseEntity<List<DoctorPatientAssociation>> getDoctorPatients(@PathVariable UUID doctorId) {
        List<DoctorPatientAssociation> patients = associationService.getDoctorPatients(doctorId);
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/doctor/{doctorId}/patients/paged")
    public ResponseEntity<Page<DoctorPatientAssociation>> getDoctorPatientsPaged(
            @PathVariable UUID doctorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<DoctorPatientAssociation> patients = associationService.getDoctorPatientsPaged(
                doctorId, PageRequest.of(page, size));
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/patient/{patientHealthId}/doctors")
    public ResponseEntity<List<DoctorPatientAssociation>> getPatientDoctors(@PathVariable String patientHealthId) {
        List<DoctorPatientAssociation> doctors = associationService.getPatientDoctors(patientHealthId);
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/doctor/{doctorId}/count")
    public ResponseEntity<Map<String, Long>> getDoctorPatientCount(@PathVariable UUID doctorId) {
        long count = associationService.getDoctorPatientCount(doctorId);
        return ResponseEntity.ok(Map.of("totalPatients", count));
    }

    @PutMapping("/{associationId}/notes")
    public ResponseEntity<DoctorPatientAssociation> updateNotes(
            @PathVariable UUID associationId,
            @RequestBody Map<String, String> request) {
        String notes = request.get("notes");
        DoctorPatientAssociation updated = associationService.updateNotes(associationId, notes);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{associationId}")
    public ResponseEntity<Void> deactivateAssociation(@PathVariable UUID associationId) {
        associationService.deactivateAssociation(associationId);
        return ResponseEntity.noContent().build();
    }
}
