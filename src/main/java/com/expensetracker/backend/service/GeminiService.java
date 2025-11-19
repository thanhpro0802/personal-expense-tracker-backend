package com.expensetracker.backend.service;

import com.expensetracker.backend.dto.AIChatRequest;
import com.expensetracker.backend.model.Transaction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GeminiService() {
        this.restClient = RestClient.create();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Build a prompt that includes Context/History + Transactions + Current Question
     */
    private String buildPrompt(List<Transaction> transactions, String question, List<AIChatRequest.HistoryMessage> history) {
        StringBuilder prompt = new StringBuilder();

        // 1. System Instruction
        prompt.append("System: You are a smart financial advisor. Analyze the provided transaction history to answer the user's question.\n");
        prompt.append("Rules:\n");
        prompt.append("- Format response in Markdown.\n");
        prompt.append("- Be concise and helpful.\n");
        prompt.append("Current Date: ").append(LocalDate.now()).append("\n\n");

        // 2. Conversation History (Context)
        if (history != null && !history.isEmpty()) {
            prompt.append("--- Conversation History ---\n");
            for (AIChatRequest.HistoryMessage msg : history) {
                String role = "user".equalsIgnoreCase(msg.getRole()) ? "User" : "AI";
                // Chỉ lấy nội dung, đảm bảo không null
                String content = msg.getContent() != null ? msg.getContent() : "";
                prompt.append(role).append(": ").append(content).append("\n");
            }
            prompt.append("--- End History ---\n\n");
        }

        // 3. Transaction Data
        prompt.append("--- Transaction Data ---\n");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (transactions.isEmpty()) {
            prompt.append("No recent transactions found.\n");
        } else {
            for (Transaction txn : transactions) {
                String sign = txn.getType() == Transaction.TransactionType.income ? "+" : "-";
                BigDecimal amount = txn.getAmount();
                String date = txn.getDate().format(formatter);
                String category = txn.getCategory();
                String title = txn.getTitle();

                prompt.append(String.format("%s: %s%s$ (%s) - %s\n",
                        date, sign, amount, category, title));
            }
        }

        prompt.append("\n");
        prompt.append("--- User Question ---\n");
        prompt.append(question);

        return prompt.toString();
    }

    /**
     * Call Google Gemini API with History support
     */
    public String getAIResponse(List<Transaction> transactions, String question, List<AIChatRequest.HistoryMessage> history) {
        try {
            String prompt = buildPrompt(transactions, question, history);

            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, Object> part = new HashMap<>();

            part.put("text", prompt);
            content.put("parts", new Object[]{part});
            requestBody.put("contents", new Object[]{content});

            logger.info("Calling Gemini API with prompt length: {}", prompt.length());

            // Make API call
            String response = restClient.post()
                    .uri(apiUrl + "?key=" + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            // Parse response
            String aiResponse = extractTextFromResponse(response);
            logger.info("Received AI response length: {}", aiResponse != null ? aiResponse.length() : 0);

            return aiResponse;

        } catch (Exception e) {
            logger.error("Error calling Gemini API", e);
            return "Sorry, I encountered an error processing your request. Please try again later.";
        }
    }

    private String extractTextFromResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode content = firstCandidate.path("content");
                JsonNode parts = content.path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    return parts.get(0).path("text").asText();
                }
            }
            return "Unable to parse AI response.";
        } catch (Exception e) {
            logger.error("Error parsing Gemini response", e);
            return "Error parsing AI response.";
        }
    }
}