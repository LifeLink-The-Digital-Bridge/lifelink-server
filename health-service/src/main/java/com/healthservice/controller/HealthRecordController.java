package com.healthservice.controller;

import com.healthservice.dto.HealthRecordCommentDTO;
import com.healthservice.dto.HealthRecordCommentRequest;
import com.healthservice.dto.DocumentUploadResponse;
import com.healthservice.dto.HealthRecordDTO;
import com.healthservice.dto.HealthRecordRequest;
import com.healthservice.service.HealthDocumentService;
import com.healthservice.service.HealthRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/health/records")
@RequiredArgsConstructor
public class HealthRecordController {

    private final HealthRecordService healthRecordService;
    private final HealthDocumentService healthDocumentService;

    @PostMapping
    public ResponseEntity<HealthRecordDTO> createHealthRecord(@Valid @RequestBody HealthRecordRequest request) {
        HealthRecordDTO record = healthRecordService.createHealthRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(record);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HealthRecordDTO> getHealthRecordById(@PathVariable UUID id) {
        HealthRecordDTO record = healthRecordService.getHealthRecordById(id);
        return ResponseEntity.ok(record);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<HealthRecordDTO>> getHealthRecordsByUserId(@PathVariable UUID userId) {
        List<HealthRecordDTO> records = healthRecordService.getHealthRecordsByUserId(userId);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/health-id/{healthId}")
    public ResponseEntity<List<HealthRecordDTO>> getHealthRecordsByHealthId(@PathVariable String healthId) {
        List<HealthRecordDTO> records = healthRecordService.getHealthRecordsByHealthId(healthId);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/user/{userId}/paginated")
    public ResponseEntity<Page<HealthRecordDTO>> getHealthRecordsPaginated(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("recordDate").descending());
        Page<HealthRecordDTO> records = healthRecordService.getHealthRecordsByUserIdPaginated(userId, pageable);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/user/{userId}/timeline")
    public ResponseEntity<List<HealthRecordDTO>> getHealthTimeline(@PathVariable UUID userId) {
        List<HealthRecordDTO> timeline = healthRecordService.getHealthRecordsByUserId(userId);
        return ResponseEntity.ok(timeline);
    }

    @GetMapping("/user/{userId}/date-range")
    public ResponseEntity<List<HealthRecordDTO>> getHealthRecordsByDateRange(
            @PathVariable UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<HealthRecordDTO> records = healthRecordService.getHealthRecordsByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/user/{userId}/emergency")
    public ResponseEntity<List<HealthRecordDTO>> getEmergencyRecords(@PathVariable UUID userId) {
        List<HealthRecordDTO> records = healthRecordService.getEmergencyRecords(userId);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/doctor/{doctorId}/emergency")
    public ResponseEntity<List<HealthRecordDTO>> getDoctorEmergencyRecords(@PathVariable UUID doctorId) {
        List<HealthRecordDTO> records = healthRecordService.getDoctorEmergencyRecords(doctorId);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/ngo/{ngoId}/emergency")
    public ResponseEntity<List<HealthRecordDTO>> getNGOEmergencyRecords(@PathVariable UUID ngoId) {
        List<HealthRecordDTO> records = healthRecordService.getNGOEmergencyRecords(ngoId);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Map<String, Long>> getRecordCount(@PathVariable UUID userId) {
        long count = healthRecordService.getRecordCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HealthRecordDTO> updateHealthRecord(
            @PathVariable UUID id,
            @Valid @RequestBody HealthRecordRequest request) {
        HealthRecordDTO updated = healthRecordService.updateHealthRecord(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHealthRecord(
            @PathVariable UUID id,
            @RequestParam UUID doctorId) {
        healthRecordService.deleteHealthRecord(id, doctorId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{recordId}/comments")
    public ResponseEntity<List<HealthRecordCommentDTO>> getHealthRecordComments(@PathVariable UUID recordId) {
        List<HealthRecordCommentDTO> comments = healthRecordService.getHealthRecordComments(recordId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/{recordId}/comments")
    public ResponseEntity<HealthRecordCommentDTO> addHealthRecordComment(
            @PathVariable UUID recordId,
            @Valid @RequestBody HealthRecordCommentRequest request) {
        HealthRecordCommentDTO comment = healthRecordService.addHealthRecordComment(recordId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @PostMapping(value = "/upload-document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentUploadResponse> uploadHealthDocument(
            @RequestParam("file") MultipartFile file) {
        DocumentUploadResponse response = healthDocumentService.uploadDocument(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/documents/{fileName:.+}")
    public ResponseEntity<Resource> getHealthDocument(@PathVariable String fileName) {
        HealthDocumentService.StoredDocument document = healthDocumentService.loadDocument(fileName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType(document.contentType()))
                .body(document.resource());
    }
}
