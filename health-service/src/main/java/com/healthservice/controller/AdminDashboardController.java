package com.healthservice.controller;

import com.healthservice.dto.SdgDashboardDTO;
import com.healthservice.service.AdminSdgDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminSdgDashboardService adminSdgDashboardService;

    @GetMapping("/sdg-dashboard")
    public ResponseEntity<SdgDashboardDTO> getSdgDashboard(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "roles", required = false) String rolesHeader) {
        validateAdminAccess(rolesHeader);
        if (authorization == null || authorization.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization token is required");
        }
        return ResponseEntity.ok(adminSdgDashboardService.getDashboard(authorization, rolesHeader));
    }

    private void validateAdminAccess(String rolesHeader) {
        if (rolesHeader == null || rolesHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
        boolean isAdmin = Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .anyMatch(role -> "ADMIN".equalsIgnoreCase(role));
        if (!isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
    }
}
