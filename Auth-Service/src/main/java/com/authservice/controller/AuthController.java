package com.authservice.controller;

import com.authservice.dto.*;
import com.authservice.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login/email")
    public ResponseEntity<AuthResponse> loginViaEmail(@RequestBody LoginRequestEmail request) {
        return ResponseEntity.ok(authService.loginViaEmail(request));
    }
    @PostMapping("/login/username")
    public ResponseEntity<AuthResponse> loginViaUsername(@RequestBody LoginRequestUsername request) {
        return ResponseEntity.ok(authService.loginViaUsername(request));
    }

    @GetMapping("/validate")
    public ResponseEntity<ValidationUserResponse> validateToken(@RequestParam String token) {
        return ResponseEntity.ok(authService.validateTokenAndUser(token));
    }
}
