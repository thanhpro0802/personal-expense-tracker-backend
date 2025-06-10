package com.expensetracker.backend.controller;

import com.expensetracker.backend.model.Budget;
import com.expensetracker.backend.service.BudgetService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    // Lấy tất cả ngân sách (cho mục đích admin hoặc test)
    @GetMapping("/all")
    public ResponseEntity<List<Budget>> getAllBudgets() {
        List<Budget> budgets = budgetService.getAllBudgets();
        return ResponseEntity.ok(budgets);
    }

    // Lấy ngân sách theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Budget> getBudgetById(@PathVariable UUID id) {
        return budgetService.getBudgetById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Lấy tất cả ngân sách của một người dùng
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Budget>> getUserBudgets(@PathVariable UUID userId) {
        try {
            List<Budget> budgets = budgetService.getUserBudgets(userId);
            return ResponseEntity.ok(budgets);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // User not found
        }
    }

    // Lấy ngân sách của một người dùng trong khoảng thời gian cụ thể
    @GetMapping("/user/{userId}/by-period")
    public ResponseEntity<List<Budget>> getUserBudgetsInPeriod(
            @PathVariable UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<Budget> budgets = budgetService.getUserBudgetsInPeriod(userId, startDate, endDate);
            return ResponseEntity.ok(budgets);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // User not found
        }
    }

    // Tạo ngân sách mới
    @PostMapping("/user/{userId}")
    public ResponseEntity<Budget> createBudget(@PathVariable UUID userId, @RequestBody Budget budget) {
        try {
            // Thiết lập User cho budget
            budget.setUser(new com.expensetracker.backend.model.User());
            budget.getUser().setId(userId);

            Budget createdBudget = budgetService.createBudget(budget);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBudget);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // User or Category not found
            }
            if (e.getMessage().contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(null); // 409 Conflict
            }
            if (e.getMessage().contains("does not belong to this user")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // 400 Bad Request
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Cập nhật ngân sách
    @PutMapping("/{id}/user/{userId}")
    public ResponseEntity<Budget> updateBudget(@PathVariable UUID id, @RequestBody Budget budgetDetails, @PathVariable UUID userId) {
        try {
            Budget updatedBudget = budgetService.updateBudget(id, budgetDetails, userId);
            return ResponseEntity.ok(updatedBudget);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            if (e.getMessage().contains("Access Denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            if (e.getMessage().contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(null); // 409 Conflict
            }
            if (e.getMessage().contains("does not belong to this user")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Xóa ngân sách
    @DeleteMapping("/{id}/user/{userId}")
    public ResponseEntity<Void> deleteBudget(@PathVariable UUID id, @PathVariable UUID userId) {
        try {
            budgetService.deleteBudget(id, userId);
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