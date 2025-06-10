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

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // Lấy tất cả giao dịch (cho mục đích admin hoặc test)
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

    // Lấy tất cả giao dịch của một người dùng
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Transaction>> getUserTransactions(@PathVariable UUID userId) {
        try {
            List<Transaction> transactions = transactionService.getUserTransactions(userId);
            return ResponseEntity.ok(transactions);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // User not found
        }
    }

    // Lấy giao dịch của một người dùng trong khoảng thời gian cụ thể
    @GetMapping("/user/{userId}/by-date")
    public ResponseEntity<List<Transaction>> getUserTransactionsBetweenDates(
            @PathVariable UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<Transaction> transactions = transactionService.getUserTransactionsBetweenDates(userId, startDate, endDate);
            return ResponseEntity.ok(transactions);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // User not found
        }
    }

    // Tạo giao dịch mới
    @PostMapping("/user/{userId}")
    public ResponseEntity<Transaction> createTransaction(@PathVariable UUID userId, @RequestBody Transaction transaction) {
        try {
            // Thiết lập User cho transaction
            transaction.setUser(new com.expensetracker.backend.model.User());
            transaction.getUser().setId(userId);

            Transaction createdTransaction = transactionService.createTransaction(transaction);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTransaction);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // User or Category not found
            }
            if (e.getMessage().contains("does not belong to this user") || e.getMessage().contains("Transaction type must match category type")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // 400 Bad Request
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Cập nhật giao dịch
    @PutMapping("/{id}/user/{userId}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable UUID id, @RequestBody Transaction transactionDetails, @PathVariable UUID userId) {
        try {
            Transaction updatedTransaction = transactionService.updateTransaction(id, transactionDetails, userId);
            return ResponseEntity.ok(updatedTransaction);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            if (e.getMessage().contains("Access Denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            if (e.getMessage().contains("does not belong to this user") || e.getMessage().contains("Transaction type must match category type")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Xóa giao dịch
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