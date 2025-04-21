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

    public AuthResponse login(UnifiedLoginRequest request) {
        UserDTO user;
        if ("email".equalsIgnoreCase(request.getLoginType())) {
            user = userClient.getUserByEmail(request.getIdentifier());
        } else if ("username".equalsIgnoreCase(request.getLoginType())) {
            user = userClient.getUserByUsername(request.getIdentifier());
        } else {
            throw new IllegalArgumentException("Invalid loginType: " + request.getLoginType());
        }

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getUsername(), user.getRoles());
    }

    public ValidationUserResponse validateTokenAndUser(String token) {
        if (!jwtUtil.isTokenValid(token)) {
            throw new TokenInvalidException("Invalid or expired token");
        }

        UUID userId = jwtUtil.extractUserId(token);
        UserDTO userDTO = userClient.getUserById(userId);
        if (userDTO == null) {
            throw new UserNotFoundException("User not found for ID: " + userId);
        }

        ValidationUserResponse response = new ValidationUserResponse();
        BeanUtils.copyProperties(userDTO, response);
        return response;
    }
}
