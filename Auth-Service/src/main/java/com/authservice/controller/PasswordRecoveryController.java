package com.authservice.controller;

import com.authservice.dto.ChangePasswordRequest;
import com.authservice.dto.EmailRequest;
import com.authservice.dto.JwtResponse;
import com.authservice.dto.OtpVerificationRequest;
import com.authservice.service.PasswordRecoveryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/password-recovery")
public class PasswordRecoveryController {

    private final PasswordRecoveryService passwordRecoveryService;

    public PasswordRecoveryController(PasswordRecoveryService passwordRecoveryService) {
        this.passwordRecoveryService = passwordRecoveryService;
    }

    @PostMapping("/request-otp")
    public ResponseEntity<?> requestOtp(@RequestBody EmailRequest request) {
        System.out.println("OTP request received for: " + request.getEmail());
        String response = passwordRecoveryService.sendOtpToEmail(request.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerificationRequest request) {
        String token = passwordRecoveryService.verifyOtp(request.getEmail(), request.getOtp());
        if (token == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired OTP");
        return ResponseEntity.ok(new JwtResponse(token));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        boolean updated = passwordRecoveryService.changePassword(request);
        if (!updated) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password mismatch or user not found");
        return ResponseEntity.ok("Password updated successfully");
    }
}
