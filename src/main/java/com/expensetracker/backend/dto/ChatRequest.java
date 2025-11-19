package com.expensetracker.backend.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public class ChatRequest {
    
    @NotBlank(message = "Question cannot be blank")
    private String question;
    
    private UUID walletId;

    public ChatRequest() {
    }

    public ChatRequest(String question, UUID walletId) {
        this.question = question;
        this.walletId = walletId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public UUID getWalletId() {
        return walletId;
    }

    public void setWalletId(UUID walletId) {
        this.walletId = walletId;
    }
}
