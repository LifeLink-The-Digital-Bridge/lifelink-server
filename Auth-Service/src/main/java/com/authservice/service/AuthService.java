package com.authservice.service;

import com.authservice.client.UserGrpcClient;
import com.authservice.dto.*;
import com.authservice.exception.InvalidCredentialsException;
import com.authservice.exception.TokenInvalidException;
import com.authservice.exception.UserNotFoundException;
import com.authservice.model.RefreshToken;
import com.authservice.util.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private final JwtUtil jwtUtil;
    private final UserGrpcClient userGrpcClient;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public AuthService(JwtUtil jwtUtil, UserGrpcClient userClient, PasswordEncoder passwordEncoder, RefreshTokenService refreshTokenService) {
        this.jwtUtil = jwtUtil;
        this.userGrpcClient = userClient;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }


    public AuthResponse login(UnifiedLoginRequest request) {
        UserDTO user;
        if ("email".equalsIgnoreCase(request.getLoginType())) {
            user = userGrpcClient.getUserByEmail(request.getIdentifier());
            System.out.println("Fetched user gender: " + user.getGender());
        } else if ("username".equalsIgnoreCase(request.getLoginType())) {
            user = userGrpcClient.getUserByUsername(request.getIdentifier());
            System.out.println("Fetched user gender: " + user.getGender());
        } else {
            throw new IllegalArgumentException("Invalid loginType: " + request.getLoginType());
        }

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String accessToken = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        refreshTokenService.createRefreshToken(user.getId(), refreshToken, jwtUtil.extractExpiration(refreshToken).toInstant());

        return new AuthResponse(accessToken, refreshToken, user.getId(), user.getEmail(), user.getGender(), user.getUsername(), user.getRoles(), user.getDob(), user.getProfileVisibility());
    }

    public AuthResponse refreshToken(String token) {
        RefreshToken refreshToken = refreshTokenService.findByToken(token)
                .orElseThrow(() -> new TokenInvalidException("Refresh token not found"));

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenService.deleteByToken(token);
            throw new TokenInvalidException("Refresh token expired");
        }

        UUID userId = jwtUtil.extractUserId(token);
        UserDTO userDTO = userGrpcClient.getUserById(userId.toString());

        String newAccessToken = jwtUtil.generateToken(userDTO);
        String newRefreshToken = jwtUtil.generateRefreshToken(userDTO);

        refreshTokenService.deleteByToken(token);
        refreshTokenService.createRefreshToken(userId, newRefreshToken, jwtUtil.extractExpiration(newRefreshToken).toInstant());

        return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                userDTO.getId(),
                userDTO.getEmail(),
                userDTO.getGender(),
                userDTO.getUsername(),
                userDTO.getRoles(),
                userDTO.getDob(),
                userDTO.getProfileVisibility()
        );
    }


    public ValidationUserResponse validateTokenAndUser(String token) {
        if (!jwtUtil.isTokenValid(token)) {
            throw new TokenInvalidException("Invalid or expired token");
        }

        UUID userId = jwtUtil.extractUserId(token);
        UserDTO userDTO = userGrpcClient.getUserById(String.valueOf(userId));
        if (userDTO == null) {
            throw new UserNotFoundException("User not found for ID: " + userId);
        }

        ValidationUserResponse response = new ValidationUserResponse();
        BeanUtils.copyProperties(userDTO, response);
        return response;
    }
}
