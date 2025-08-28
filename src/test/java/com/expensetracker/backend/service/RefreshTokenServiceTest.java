package com.expensetracker.backend.service;

import com.expensetracker.backend.exception.TokenRefreshException;
import com.expensetracker.backend.model.RefreshToken;
import com.expensetracker.backend.model.User;
import com.expensetracker.backend.repository.RefreshTokenRepository;
import com.expensetracker.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = User.builder()
                .id(testUserId)
                .username("testuser")
                .email("test@example.com")
                .name("Test User")
                .passwordHash("hashedpassword")
                .build();

        // Set the refresh token duration for testing (1 hour)
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", 3600000L);
    }

    @Test
    void createRefreshToken_Success() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken token = invocation.getArgument(0);
            token.setId(UUID.randomUUID());
            return token;
        });

        // When
        RefreshToken result = refreshTokenService.createRefreshToken(testUserId);

        // Then
        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertNotNull(result.getToken());
        assertTrue(result.getExpiryDate().isAfter(LocalDateTime.now()));
        
        verify(refreshTokenRepository).deleteByUserId(testUserId);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void createRefreshToken_UserNotFound() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> refreshTokenService.createRefreshToken(testUserId));
        assertEquals("User not found with id: " + testUserId, exception.getMessage());
    }

    @Test
    void verifyExpiration_TokenValid() {
        // Given
        RefreshToken token = RefreshToken.builder()
                .token("valid-token")
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .build();

        // When
        RefreshToken result = refreshTokenService.verifyExpiration(token);

        // Then
        assertEquals(token, result);
        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    void verifyExpiration_TokenExpired() {
        // Given
        RefreshToken expiredToken = RefreshToken.builder()
                .token("expired-token")
                .user(testUser)
                .expiryDate(LocalDateTime.now().minusHours(1))
                .build();

        // When & Then
        TokenRefreshException exception = assertThrows(TokenRefreshException.class,
                () -> refreshTokenService.verifyExpiration(expiredToken));
        
        assertTrue(exception.getMessage().contains("Refresh token was expired"));
        verify(refreshTokenRepository).delete(expiredToken);
    }

    @Test
    void findByToken_Success() {
        // Given
        String tokenValue = "test-token";
        RefreshToken token = RefreshToken.builder()
                .token(tokenValue)
                .user(testUser)
                .build();
        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));

        // When
        Optional<RefreshToken> result = refreshTokenService.findByToken(tokenValue);

        // Then
        assertTrue(result.isPresent());
        assertEquals(token, result.get());
    }

    @Test
    void deleteByUserId_Success() {
        // When
        refreshTokenService.deleteByUserId(testUserId);

        // Then
        verify(refreshTokenRepository).deleteByUserId(testUserId);
    }
}