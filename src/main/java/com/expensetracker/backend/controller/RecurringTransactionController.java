package com.expensetracker.backend.controller;

import com.expensetracker.backend.model.RecurringTransaction;
import com.expensetracker.backend.security.services.UserDetailsImpl;
import com.expensetracker.backend.service.RecurringTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/recurring-transactions")
public class RecurringTransactionController {

    @Autowired
    private RecurringTransactionService recurringService;

    @GetMapping
    public ResponseEntity<List<RecurringTransaction>> getAllRecurringTransactions(
            @RequestParam UUID walletId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<RecurringTransaction> transactions = recurringService.getAllForWallet(walletId, userDetails.getId());
        return ResponseEntity.ok(transactions);
    }

    @PostMapping
    public ResponseEntity<RecurringTransaction> createRecurringTransaction(
            @RequestParam UUID walletId,
            @RequestBody RecurringTransaction rt,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        RecurringTransaction created = recurringService.createRecurringTransaction(rt, walletId, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecurringTransaction> updateRecurringTransaction(
            @PathVariable UUID id,
            @RequestParam UUID walletId,
            @RequestBody RecurringTransaction details,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            RecurringTransaction updated = recurringService.updateRecurringTransaction(id, details, walletId, userDetails.getId());
            return ResponseEntity.ok(updated);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecurringTransaction(
            @PathVariable UUID id,
            @RequestParam UUID walletId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            recurringService.deleteRecurringTransaction(id, walletId, userDetails.getId());
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}