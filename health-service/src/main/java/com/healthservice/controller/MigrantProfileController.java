package com.healthservice.controller;

import com.healthservice.dto.MigrantProfileDTO;
import com.healthservice.service.MigrantProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/migrant-profile")
@RequiredArgsConstructor
public class MigrantProfileController {

    private final MigrantProfileService migrantProfileService;

    @PostMapping
    public ResponseEntity<MigrantProfileDTO> createMigrantProfile(@Valid @RequestBody MigrantProfileDTO dto) {
        MigrantProfileDTO profile = migrantProfileService.createMigrantProfile(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(profile);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<MigrantProfileDTO> getMigrantProfileByUserId(@PathVariable UUID userId) {
        MigrantProfileDTO profile = migrantProfileService.getMigrantProfileByUserId(userId);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/health-id/{healthId}")
    public ResponseEntity<MigrantProfileDTO> getMigrantProfileByHealthId(@PathVariable String healthId) {
        MigrantProfileDTO profile = migrantProfileService.getMigrantProfileByHealthId(healthId);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/user/{userId}")
    public ResponseEntity<MigrantProfileDTO> updateMigrantProfile(
            @PathVariable UUID userId,
            @Valid @RequestBody MigrantProfileDTO dto) {
        MigrantProfileDTO updated = migrantProfileService.updateMigrantProfile(userId, dto);
        return ResponseEntity.ok(updated);
    }
}
