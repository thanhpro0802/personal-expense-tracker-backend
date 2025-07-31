package com.expensetracker.backend.payload.response;

import java.util.UUID;

public class JwtResponse {
    private String token; // Đây là Access Token
    private String type = "Bearer";
    private String refreshToken; // Thêm trường refreshToken
    private UUID id;
    private String username;
    private String email;

    // Cập nhật constructor để nhận cả refreshToken
    public JwtResponse(String accessToken, String refreshToken, UUID id, String username, String email) {
        this.token = accessToken;
        this.refreshToken = refreshToken;
        this.id = id;
        this.username = username;
        this.email = email;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}