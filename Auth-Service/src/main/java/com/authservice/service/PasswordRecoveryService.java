package com.authservice.service;

import com.authservice.dto.ChangePasswordRequest;

public interface PasswordRecoveryService {
    String sendOtpToEmail(String email);

    String verifyOtp(String email, String otp);

    boolean changePassword(ChangePasswordRequest request);
}