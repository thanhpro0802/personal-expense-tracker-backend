package com.expensetracker.backend.controller;

import com.expensetracker.backend.dto.BudgetDTO;
import com.expensetracker.backend.model.Budget;
import com.expensetracker.backend.security.services.UserDetailsImpl;
import com.expensetracker.backend.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    // Helper method để lấy User ID từ principal
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }

    /**
     * Lấy tất cả ngân sách (và chi tiêu thực tế) cho một tháng/năm.
     * VD: GET /api/budgets?month=10&year=2025
     */
    @GetMapping
    public ResponseEntity<List<BudgetDTO>> getBudgets(
            @RequestParam int month,
            @RequestParam int year) {

        UUID userId = getCurrentUserId();
        List<BudgetDTO> budgets = budgetService.getBudgetsForMonth(userId, month, year);
        return ResponseEntity.ok(budgets);
    }

    /**
     * Tạo hoặc cập nhật một ngân sách.
     * POST /api/budgets
     * Body: { "category": "Ăn uống", "amount": 5000000, "month": 10, "year": 2025 }
     */
    @PostMapping
    public ResponseEntity<Budget> createOrUpdateBudget(@RequestBody Budget budget) {
        UUID userId = getCurrentUserId();
        Budget savedBudget = budgetService.createOrUpdateBudget(budget, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBudget);
    }

    /**
     * Xóa một ngân sách.
     * DELETE /api/budgets/uuid-cua-budget
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable UUID id) {
        UUID userId = getCurrentUserId();
        try {
            budgetService.deleteBudget(id, userId);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}