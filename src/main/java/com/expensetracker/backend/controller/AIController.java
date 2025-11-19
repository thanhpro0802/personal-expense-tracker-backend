package com.expensetracker.backend.controller;

import com.expensetracker.backend.dto.ApiResponse;
import com.expensetracker.backend.dto.ChatRequest;
import com.expensetracker.backend.security.services.UserDetailsImpl;
import com.expensetracker.backend.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final GeminiService geminiService;

    @Autowired
    public AIController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            throw new IllegalStateException("User is not authenticated or principal is of unexpected type.");
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<String>> chat(@RequestBody ChatRequest chatRequest) {
        try {
            UUID userId = getCurrentUserId();
            String response = geminiService.getChatResponse(
                chatRequest.getQuestion(), 
                chatRequest.getWalletId(), 
                userId
            );
            return ResponseEntity.ok(new ApiResponse<>(true, response));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(false, "Access denied: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error: " + e.getMessage()));
        }
    }
}
