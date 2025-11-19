package com.expensetracker.backend.service;

import com.expensetracker.backend.dto.GeminiDto;
import com.expensetracker.backend.model.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestClient restClient;

    public GeminiService() {
        this.restClient = RestClient.builder().build();
    }

    public String generateFinancialAdvice(String userQuestion, List<Transaction> transactions) {
        String transactionSummary = buildTransactionSummary(transactions);
        String prompt = buildPrompt(userQuestion, transactionSummary);

        GeminiDto.GeminiRequest request = createGeminiRequest(prompt);

        try {
            GeminiDto.GeminiResponse response = restClient.post()
                    .uri(apiUrl + "?key=" + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(GeminiDto.GeminiResponse.class);

            if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
                GeminiDto.Candidate candidate = response.getCandidates().get(0);
                if (candidate.getContent() != null && candidate.getContent().getParts() != null 
                        && !candidate.getContent().getParts().isEmpty()) {
                    return candidate.getContent().getParts().get(0).getText();
                }
            }
            return "I'm sorry, I couldn't generate a response at this time.";
        } catch (Exception e) {
            throw new RuntimeException("Failed to communicate with Gemini API: " + e.getMessage(), e);
        }
    }

    private String buildTransactionSummary(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            return "No transactions found for the current month.";
        }

        StringBuilder summary = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        summary.append("Transaction Summary:\n");
        for (Transaction transaction : transactions) {
            summary.append(String.format("- %s: %s %s (Category: %s, Date: %s)\n",
                    transaction.getType().toString().toUpperCase(),
                    transaction.getAmount(),
                    transaction.getTitle(),
                    transaction.getCategory(),
                    transaction.getDate().format(formatter)));
        }

        return summary.toString();
    }

    private String buildPrompt(String userQuestion, String transactionSummary) {
        return String.format("User Question: %s\n\nContext:\n%s\n\nPlease provide a helpful financial analysis and advice based on the transactions above.",
                userQuestion, transactionSummary);
    }

    private GeminiDto.GeminiRequest createGeminiRequest(String prompt) {
        GeminiDto.Part promptPart = new GeminiDto.Part(prompt);
        GeminiDto.Content content = new GeminiDto.Content(List.of(promptPart));

        String systemInstructionText = "You are a helpful financial advisor. Analyze the user's transactions and provide clear, actionable financial advice. Format your response in Markdown for better readability. Be concise but thorough in your analysis.";
        GeminiDto.Part systemPart = new GeminiDto.Part(systemInstructionText);
        GeminiDto.SystemInstruction systemInstruction = new GeminiDto.SystemInstruction(List.of(systemPart));

        return new GeminiDto.GeminiRequest(List.of(content), systemInstruction);
    }
}
