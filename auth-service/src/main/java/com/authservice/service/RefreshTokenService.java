package com.authservice.service;

import com.authservice.model.RefreshToken;
import com.authservice.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository repository) {
        this.refreshTokenRepository = repository;
    }

    public RefreshToken createRefreshToken(UUID userId, String tokenStr, Instant expiry) {
        RefreshToken token = new RefreshToken();
        token.setUserId(userId);
        token.setToken(tokenStr);
        token.setExpiryDate(expiry);
        return refreshTokenRepository.save(token);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public void deleteByToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }
}
