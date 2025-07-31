package com.expensetracker.backend.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupRequest {
    @NotBlank
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email(message = "Email format is invalid")
    private String email;

    @NotBlank
    @Size(min = 6, max = 40, message = "Password must be between 6 and 40 characters")
    private String password;
}