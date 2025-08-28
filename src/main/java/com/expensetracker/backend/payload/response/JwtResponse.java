package com.expensetracker.backend.payload.response;

import lombok.Data;

import java.util.UUID;

@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer"; // Loại token
    private String refreshToken;
    private UUID id;
    private String username;
    private String email;

    public JwtResponse(String accessToken, String refreshToken, UUID id, String username, String email) {
        this.token = accessToken;
        this.refreshToken = refreshToken;
        this.id = id;
        this.username = username;
        this.email = email;
    }

    // Keep backward compatibility with old constructor
    public JwtResponse(String accessToken, UUID id, String username, String email) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.email = email;
    }

    // Getters và Setters sẽ được Lombok tạo tự động
}