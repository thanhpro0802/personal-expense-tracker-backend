package com.expensetracker.backend.dto;

public class AIChatResponse {
    private String response;

    public AIChatResponse() {
    }

    public AIChatResponse(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
