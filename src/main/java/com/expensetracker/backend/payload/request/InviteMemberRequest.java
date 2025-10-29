package com.expensetracker.backend.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InviteMemberRequest {
    
    @NotBlank(message = "Username is required")
    private String username;
}
