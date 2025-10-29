package com.expensetracker.backend.controller;

import com.expensetracker.backend.dto.ApiResponse;
import com.expensetracker.backend.dto.DashboardStats;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private DashboardService dashboardService;

    @GetMapping(value = "/stats", produces = "application/json")
    public ResponseEntity<?> getStats(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam UUID walletId,
            @RequestParam int month,
            @RequestParam int year) {

        if (userDetails == null) {
            logger.warn("UserDetails is null, returning UNAUTHORIZED");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UUID userId = userDetails.getId();
        logger.info("Fetching stats for userId: {}, walletId: {}, month: {}, year: {}", userId, walletId, month, year);

        try {
            DashboardStats stats = dashboardService.getDashboardStats(walletId, userId, month, year);
            logger.info("Stats fetched successfully for userId: {} and walletId: {}", userId, walletId);
            return ResponseEntity.ok(new ApiResponse<>(true, stats));
        } catch (SecurityException e) {
            logger.warn("Access denied for userId: {} to walletId: {}", userId, walletId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}