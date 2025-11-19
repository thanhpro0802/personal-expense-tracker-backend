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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

        // Lấy 100 giao dịch gần nhất để AI phân tích
        Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "date"));
        List<Transaction> transactions = transactionRepository
                .findByUser_Id(userId, pageable)
                .getContent();

        logger.info("Retrieved {} recent transactions for userId: {}", transactions.size(), userId);

        // --- GỌI SERVICE VỚI HISTORY ---
        // Truyền thêm request.getHistory() vào hàm service
        String aiResponse = geminiService.getAIResponse(transactions, question, request.getHistory());

        AIChatResponse response = new AIChatResponse(aiResponse);
        return ResponseEntity.ok(new ApiResponse<>(true, response));
    }
}