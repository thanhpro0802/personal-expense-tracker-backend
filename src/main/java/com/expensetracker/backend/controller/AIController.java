package com.expensetracker.backend.controller;

import com.expensetracker.backend.dto.ApiResponse;
import com.expensetracker.backend.dto.ChatRequest;
import com.expensetracker.backend.model.Transaction;
import com.expensetracker.backend.security.services.UserDetailsImpl;
import com.expensetracker.backend.service.GeminiService;
import com.expensetracker.backend.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final GeminiService geminiService;
    private final TransactionService transactionService;

    @Autowired
    public AIController(GeminiService geminiService, TransactionService transactionService) {
        this.geminiService = geminiService;
        this.transactionService = transactionService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<String>> chat(@Valid @RequestBody ChatRequest chatRequest) {
        UUID userId = getCurrentUserId();
        
        // Get current month's transactions
        YearMonth currentMonth = YearMonth.now();
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();
        
        // Fetch all transactions for the current month (use a large page size)
        Page<Transaction> transactionsPage = transactionService.getFilteredTransactions(
                chatRequest.getWalletId(),
                userId,
                null, // type - all types
                null, // category - all categories
                null, // search
                startDate,
                endDate,
                0, // page
                1000, // size - get up to 1000 transactions
                new String[]{"date", "desc"} // sort by date descending
        );
        
        List<Transaction> transactions = transactionsPage.getContent();
        
        String advice = geminiService.generateFinancialAdvice(chatRequest.getQuestion(), transactions);
        
        return ResponseEntity.ok(new ApiResponse<>(true, advice));
    }

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            throw new IllegalStateException("User is not authenticated or principal is of unexpected type.");
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }
}
