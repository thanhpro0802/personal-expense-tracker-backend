package com.expensetracker.backend.controller;

import com.expensetracker.backend.model.Transaction;
import com.expensetracker.backend.service.TransactionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // Lấy tất cả giao dịch (chỉ dùng test/admin)
    @GetMapping("/all")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        List<Transaction> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    // Lấy giao dịch theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable UUID id) {
        return transactionService.getTransactionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Lấy tất cả giao dịch của user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Transaction>> getUserTransactions(@PathVariable UUID userId) {
        List<Transaction> transactions = transactionService.getUserTransactions(userId);
        return ResponseEntity.ok(transactions);
    }

    // Lấy giao dịch của user trong khoảng ngày
    @GetMapping("/user/{userId}/by-date")
    public ResponseEntity<List<Transaction>> getUserTransactionsBetweenDates(
            @PathVariable UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Transaction> transactions = transactionService.getUserTransactionsBetweenDates(userId, startDate, endDate);
        return ResponseEntity.ok(transactions);
    }

    // Tạo giao dịch mới cho user
    @PostMapping("/user/{userId}")
    public ResponseEntity<Transaction> createTransaction(@PathVariable UUID userId, @RequestBody Transaction transaction) {
        transaction.setUser(new com.expensetracker.backend.model.User());
        transaction.getUser().setId(userId);

        Transaction created = transactionService.createTransaction(transaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Cập nhật giao dịch của user
    @PutMapping("/{id}/user/{userId}")
    public ResponseEntity<Transaction> updateTransaction(
            @PathVariable UUID id,
            @RequestBody Transaction transactionDetails,
            @PathVariable UUID userId) {

        Transaction updated = transactionService.updateTransaction(id, transactionDetails, userId);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}/user/{userId}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable UUID id, @PathVariable UUID userId) {
        try {
            transactionService.deleteTransaction(id, userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            if (e.getMessage().contains("Access Denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}