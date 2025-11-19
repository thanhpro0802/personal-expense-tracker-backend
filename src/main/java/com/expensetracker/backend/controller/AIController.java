package com.expensetracker.backend.controller;

import com.expensetracker.backend.dto.AIChatRequest;
import com.expensetracker.backend.dto.AIChatResponse;
import com.expensetracker.backend.dto.ApiResponse;
import com.expensetracker.backend.model.Transaction;
import com.expensetracker.backend.repository.TransactionRepository;
import com.expensetracker.backend.security.services.UserDetailsImpl;
import com.expensetracker.backend.service.GeminiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private static final Logger logger = LoggerFactory.getLogger(AIController.class);

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private TransactionRepository transactionRepository;

    @PostMapping("/chat")
    public ResponseEntity<?> chat(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody AIChatRequest request) {

        if (userDetails == null) {
            logger.warn("UserDetails is null, returning UNAUTHORIZED");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UUID userId = userDetails.getId();
        String question = request.getQuestion();

        if (question == null || question.trim().isEmpty()) {
            logger.warn("Question is empty for userId: {}", userId);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Question cannot be empty"));
        }

        logger.info("Received AI chat request from userId: {}", userId);

        // Get current month and year
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        // Retrieve current month's transactions (get all transactions without pagination)
        List<Transaction> transactions = transactionRepository
                .findRecentTransactionsByMonthAndYear(userId, currentMonth, currentYear, PageRequest.of(0, Integer.MAX_VALUE));

        logger.info("Retrieved {} transactions for userId: {} (month: {}, year: {})",
                transactions.size(), userId, currentMonth, currentYear);

        // Get AI response from Gemini
        String aiResponse = geminiService.getAIResponse(transactions, question);

        AIChatResponse response = new AIChatResponse(aiResponse);
        return ResponseEntity.ok(new ApiResponse<>(true, response));
    }
}
