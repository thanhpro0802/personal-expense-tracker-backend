package com.expensetracker.backend.controller;

import com.expensetracker.backend.model.Wallet;
import com.expensetracker.backend.security.services.UserDetailsImpl;
import com.expensetracker.backend.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;

    @Autowired
    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            throw new IllegalStateException("User is not authenticated or principal is of unexpected type.");
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }

    /**
     * Lấy tất cả ví của người dùng
     */
    @GetMapping
    public ResponseEntity<List<Wallet>> getWallets() {
        UUID userId = getCurrentUserId();
        List<Wallet> wallets = walletService.getWalletsForUser(userId);
        return ResponseEntity.ok(wallets);
    }

    /**
     * Tạo ví mới
     */
    @PostMapping
    public ResponseEntity<Wallet> createWallet(@RequestBody Map<String, String> request) {
        UUID userId = getCurrentUserId();
        String name = request.get("name");
        
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        Wallet wallet = walletService.createWallet(name, userId);
        return new ResponseEntity<>(wallet, HttpStatus.CREATED);
    }

    /**
     * Mời người dùng vào ví
     */
    @PostMapping("/{walletId}/invite")
    public ResponseEntity<Wallet> inviteUser(
            @PathVariable UUID walletId,
            @RequestBody Map<String, String> request) {
        UUID userId = getCurrentUserId();
        String inviteeEmail = request.get("email");
        
        if (inviteeEmail == null || inviteeEmail.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            Wallet wallet = walletService.inviteUserToWallet(walletId, inviteeEmail, userId);
            return ResponseEntity.ok(wallet);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
