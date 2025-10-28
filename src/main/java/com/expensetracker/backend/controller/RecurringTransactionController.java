package com.expensetracker.backend.controller;

import com.expensetracker.backend.model.RecurringTransaction;
import com.expensetracker.backend.security.services.UserDetailsImpl;
import com.expensetracker.backend.service.RecurringTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/recurring-transactions")
public class RecurringTransactionController {

    @Autowired
    private RecurringTransactionService recurringService;

    // Helper method để lấy User ID từ principal
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }

    /**
     * Lấy tất cả các cài đặt giao dịch định kỳ của người dùng.
     * GET /api/recurring-transactions
     */
    @GetMapping
    public ResponseEntity<List<RecurringTransaction>> getAllRecurringTransactions() {
        UUID userId = getCurrentUserId();
        List<RecurringTransaction> transactions = recurringService.getAllForUser(userId);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Tạo một cài đặt giao dịch định kỳ mới.
     * POST /api/recurring-transactions
     * Body: { "title": "Tiền thuê nhà", "amount": 10000000, "category": "Nhà ở", "type": "expense",
     * "frequency": "MONTHLY", "startDate": "2025-11-01" }
     */
    @PostMapping
    public ResponseEntity<RecurringTransaction> createRecurringTransaction(@RequestBody RecurringTransaction rt) {
        UUID userId = getCurrentUserId();
        RecurringTransaction created = recurringService.createRecurringTransaction(rt, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Cập nhật một cài đặt giao dịch định kỳ.
     * PUT /api/recurring-transactions/uuid-cua-giao-dich
     */
    @PutMapping("/{id}")
    public ResponseEntity<RecurringTransaction> updateRecurringTransaction(
            @PathVariable UUID id,
            @RequestBody RecurringTransaction details) {
        UUID userId = getCurrentUserId();
        try {
            RecurringTransaction updated = recurringService.updateRecurringTransaction(id, details, userId);
            return ResponseEntity.ok(updated);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Xóa một cài đặt giao dịch định kỳ.
     * DELETE /api/recurring-transactions/uuid-cua-giao-dich
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecurringTransaction(@PathVariable UUID id) {
        UUID userId = getCurrentUserId();
        try {
            recurringService.deleteRecurringTransaction(id, userId);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}