package com.authservice.controller;

import com.authservice.dto.*;
import com.authservice.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid  @RequestBody UnifiedLoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/validate")
    public ResponseEntity<ValidationUserResponse> validateToken(@RequestParam String token) {
        return ResponseEntity.ok(authService.validateTokenAndUser(token));
    }
}
