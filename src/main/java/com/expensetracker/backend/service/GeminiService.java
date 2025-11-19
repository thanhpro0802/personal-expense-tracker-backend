package com.expensetracker.backend.service;

import com.expensetracker.backend.dto.GeminiDto;
import com.expensetracker.backend.model.Transaction;
import com.expensetracker.backend.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final TransactionRepository transactionRepository;
    private final WalletService walletService;
    private final RestClient restClient;

    @Autowired
    public GeminiService(TransactionRepository transactionRepository, 
                        WalletService walletService,
                        RestClient.Builder restClientBuilder) {
        this.transactionRepository = transactionRepository;
        this.walletService = walletService;
        this.restClient = restClientBuilder.build();
    }

    public String getChatResponse(String question, UUID walletId, UUID userId) {
        // Verify user has access to the wallet
        if (!walletService.isUserMemberOfWallet(walletId, userId)) {
            throw new SecurityException("User is not a member of this wallet");
        }

        // Get current month transactions
        YearMonth currentMonth = YearMonth.now();
        int month = currentMonth.getMonthValue();
        int year = currentMonth.getYear();

        List<Transaction> transactions = transactionRepository
            .findRecentTransactionsByMonthAndYear(walletId, month, year, null);

        // Optimize transactions into a concise summary
        String transactionSummary = optimizeTransactionData(transactions, month, year);

        // Build the system instruction
        String systemInstructionText = "You are a helpful financial advisor assistant. " +
            "Analyze the user's transaction data and provide clear, concise financial advice. " +
            "Format your response in Markdown for better readability. " +
            "Be friendly and supportive in your tone.";

        // Build the user prompt with optimized transaction data
        String userPrompt = String.format(
            "Based on my financial transactions for %d/%d:\n\n%s\n\nQuestion: %s",
            month, year, transactionSummary, question
        );

        // Build Gemini API request
        GeminiDto.Request request = buildGeminiRequest(systemInstructionText, userPrompt);

        // Call Gemini API
        try {
            String fullUrl = apiUrl + "?key=" + apiKey;
            
            GeminiDto.Response response = restClient.post()
                .uri(fullUrl)
                .header("Content-Type", "application/json")
                .body(request)
                .retrieve()
                .body(GeminiDto.Response.class);

            if (response != null && 
                response.getCandidates() != null && 
                !response.getCandidates().isEmpty() &&
                response.getCandidates().get(0).getContent() != null &&
                response.getCandidates().get(0).getContent().getParts() != null &&
                !response.getCandidates().get(0).getContent().getParts().isEmpty()) {
                
                return response.getCandidates().get(0)
                    .getContent()
                    .getParts()
                    .get(0)
                    .getText();
            }

            return "Sorry, I couldn't generate a response. Please try again.";

        } catch (Exception e) {
            throw new RuntimeException("Error calling Gemini API: " + e.getMessage(), e);
        }
    }

    private String optimizeTransactionData(List<Transaction> transactions, int month, int year) {
        if (transactions.isEmpty()) {
            return String.format("No transactions found for %d/%d.", month, year);
        }

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        // Calculate totals and build summary
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("Transaction Summary for %d/%d:\n", month, year));
        summary.append(String.format("Total transactions: %d\n\n", transactions.size()));

        summary.append("Transactions:\n");
        for (Transaction t : transactions) {
            if (t.getType() == Transaction.TransactionType.income) {
                totalIncome = totalIncome.add(t.getAmount());
            } else {
                totalExpense = totalExpense.add(t.getAmount());
            }
            
            summary.append(String.format("- %s: %s %s (%s) on %s\n",
                t.getType().toString().toUpperCase(),
                t.getAmount(),
                "USD", // You can make this dynamic if currency is stored
                t.getCategory(),
                t.getDate()
            ));
        }

        summary.append(String.format("\nTotal Income: %s USD\n", totalIncome));
        summary.append(String.format("Total Expenses: %s USD\n", totalExpense));
        summary.append(String.format("Net Balance: %s USD\n", totalIncome.subtract(totalExpense)));

        return summary.toString();
    }

    private GeminiDto.Request buildGeminiRequest(String systemInstruction, String userPrompt) {
        // Build system instruction
        GeminiDto.SystemInstruction systemInst = GeminiDto.SystemInstruction.builder()
            .parts(List.of(
                GeminiDto.Part.builder()
                    .text(systemInstruction)
                    .build()
            ))
            .build();

        // Build user content
        GeminiDto.Content userContent = GeminiDto.Content.builder()
            .role("user")
            .parts(List.of(
                GeminiDto.Part.builder()
                    .text(userPrompt)
                    .build()
            ))
            .build();

        // Build generation config
        GeminiDto.GenerationConfig config = GeminiDto.GenerationConfig.builder()
            .temperature(0.7)
            .topK(40)
            .topP(0.95)
            .maxOutputTokens(1024)
            .build();

        // Build safety settings
        List<GeminiDto.SafetySetting> safetySettings = new ArrayList<>();
        String[] categories = {
            "HARM_CATEGORY_HARASSMENT",
            "HARM_CATEGORY_HATE_SPEECH",
            "HARM_CATEGORY_SEXUALLY_EXPLICIT",
            "HARM_CATEGORY_DANGEROUS_CONTENT"
        };
        
        for (String category : categories) {
            safetySettings.add(GeminiDto.SafetySetting.builder()
                .category(category)
                .threshold("BLOCK_MEDIUM_AND_ABOVE")
                .build());
        }

        return GeminiDto.Request.builder()
            .systemInstruction(systemInst)
            .contents(List.of(userContent))
            .generationConfig(config)
            .safetySettings(safetySettings)
            .build();
    }
}
