package com.expensetracker.backend.dto;

import java.util.List;

public class AIChatRequest {
    private String question;
    private List<HistoryMessage> history;
    private String timeRange; // <--- Thêm trường này (values: "recent", "month", "year", "all")

    // Constructors
    public AIChatRequest() {}

    // Getters and Setters
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public List<HistoryMessage> getHistory() { return history; }
    public void setHistory(List<HistoryMessage> history) { this.history = history; }

    public String getTimeRange() { return timeRange; }
    public void setTimeRange(String timeRange) { this.timeRange = timeRange; }

    public static class HistoryMessage {
        private String role;
        private String content;
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}