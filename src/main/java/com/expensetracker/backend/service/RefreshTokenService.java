package com.expensetracker.backend.service;

import com.expensetracker.backend.exception.TokenRefreshException;
import com.expensetracker.backend.model.RefreshToken;
import com.expensetracker.backend.model.User;
import com.expensetracker.backend.repository.RefreshTokenRepository;
import com.expensetracker.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${jwt.refresh.expiration.ms}")
    private Long refreshTokenDurationMs;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createRefreshToken(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Delete any existing refresh token for this user
        deleteByUserId(userId);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenDurationMs / 1000))
                .token(UUID.randomUUID().toString())
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), 
                "Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    @Transactional
    public void deleteByUserId(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    @Transactional
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteAllExpiredTokens(LocalDateTime.now());
    }
}