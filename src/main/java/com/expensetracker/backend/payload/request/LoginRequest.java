package com.expensetracker.backend.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data // Lombok: Tự động tạo getters, setters, toString, equals, hashCode
public class LoginRequest {
    @NotBlank // Đảm bảo trường này không rỗng
    private String username;

    @NotBlank
    private String password;

    // Getters và Setters sẽ được Lombok tạo tự động
}