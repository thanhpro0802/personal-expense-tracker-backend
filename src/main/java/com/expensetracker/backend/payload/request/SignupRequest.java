package com.expensetracker.backend.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email // Đảm bảo định dạng email hợp lệ
    private String email;

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;

    // Getters và Setters sẽ được Lombok tạo tự động
}