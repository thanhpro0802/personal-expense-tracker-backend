package com.expensetracker.backend.controller;

import com.expensetracker.backend.dto.ApiResponse;
import com.expensetracker.backend.dto.DashboardStats;
import com.expensetracker.backend.model.Transaction;
import com.expensetracker.backend.security.services.UserDetailsImpl;
import com.expensetracker.backend.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private DashboardService dashboardService;

    @GetMapping(value = "/stats", produces = "application/json")
    public ResponseEntity<?> getStats(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        logger.info("Received request for userId: {}", userDetails != null ? userDetails.getId() : "null");
        if (userDetails == null) {
            logger.warn("UserDetails is null, returning UNAUTHORIZED");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UUID userId = userDetails.getId();
        logger.info("Fetching stats for userId: {}", userId);
        DashboardStats stats = dashboardService.getDashboardStats(userId);
        logger.info("Stats fetched successfully for userId: {}", userId);
        return ResponseEntity.ok(new ApiResponse<>(true, stats));
    }

    @GetMapping("/recent-transactions")
    public ResponseEntity<?> getRecentTransactions(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        logger.info("Received request for recent transactions for userId: {}", userDetails != null ? userDetails.getId() : "null");
        if (userDetails == null) {
            logger.warn("UserDetails is null, returning UNAUTHORIZED");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UUID userId = userDetails.getId();
        logger.info("Fetching recent transactions for userId: {}", userId);
        List<Transaction> transactions = dashboardService.getRecentTransactions(userId, 5);
        logger.info("Fetched {} recent transactions for userId: {}", transactions.size(), userId);
        return ResponseEntity.ok(new ApiResponse<>(true, transactions));
    }
}