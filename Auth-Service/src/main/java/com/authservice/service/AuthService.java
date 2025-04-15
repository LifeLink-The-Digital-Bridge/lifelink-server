package com.authservice.service;

import com.authservice.client.UserClient;
import com.authservice.dto.*;
import com.authservice.exception.InvalidCredentialsException;
import com.authservice.exception.TokenInvalidException;
import com.authservice.exception.UserNotFoundException;
import com.authservice.util.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private final JwtUtil jwtUtil;
    private final UserClient userClient;
    private final PasswordEncoder passwordEncoder;

    public AuthService(JwtUtil jwtUtil, UserClient userClient, PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.userClient = userClient;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse loginViaEmail(LoginRequestEmail request) {
        UserDTO user = userClient.getUserByEmail(request.getEmail());
        if (user == null) {
            throw new UserNotFoundException("User not found for email: " + request.getEmail());
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token,user.getId(), user.getEmail(), user.getUsername(), user.getRoles());
    }

    public AuthResponse loginViaUsername(LoginRequestUsername request) {
        UserDTO user = userClient.getUserByUsername(request.getUsername());
        if (user == null) {
            throw new UserNotFoundException("User not found for username: " + request.getUsername());
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token, user.getId(),user.getEmail(), user.getUsername(), user.getRoles());
    }

    public ValidationUserResponse validateTokenAndUser(String token) {
        if (!jwtUtil.isTokenValid(token)) {
            throw new TokenInvalidException("Invalid or expired token");
        }

        UUID userId = jwtUtil.extractUserId(token);
        try {
            ValidationUserResponse validationUserResponse = new ValidationUserResponse();
            UserDTO userDTO = userClient.getUserById(userId);
            if (userDTO == null) {
                throw new UserNotFoundException("User not found for ID: " + userId);
            }

            BeanUtils.copyProperties(userDTO, validationUserResponse);
            return validationUserResponse;
        } catch (Exception e) {
            throw new UserNotFoundException("User not found for ID: " + userId);
        }
    }
}
