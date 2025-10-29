package com.expensetracker.backend.controller;

import com.expensetracker.backend.dto.WalletDTO;
import com.expensetracker.backend.dto.WalletDetailDTO;
import com.expensetracker.backend.dto.WalletMemberDTO;
import com.expensetracker.backend.payload.request.CreateWalletRequest;
import com.expensetracker.backend.payload.request.InviteMemberRequest;
import com.expensetracker.backend.security.services.UserDetailsImpl;
import com.expensetracker.backend.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @PostMapping
    public ResponseEntity<WalletDTO> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        UUID userId = getCurrentUserId();
        WalletDTO wallet = walletService.createWallet(request.getName(), request.getType(), userId);
        return new ResponseEntity<>(wallet, HttpStatus.CREATED);
    }

    @PostMapping("/{walletId}/members")
    public ResponseEntity<WalletMemberDTO> inviteMember(
            @PathVariable UUID walletId,
            @Valid @RequestBody InviteMemberRequest request) {
        UUID userId = getCurrentUserId();
        try {
            WalletMemberDTO member = walletService.inviteMember(walletId, request.getUsername(), userId);
            return new ResponseEntity<>(member, HttpStatus.CREATED);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<WalletDTO>> getUserWallets() {
        UUID userId = getCurrentUserId();
        List<WalletDTO> wallets = walletService.getUserWallets(userId);
        return ResponseEntity.ok(wallets);
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<WalletDetailDTO> getWalletDetail(@PathVariable UUID walletId) {
        UUID userId = getCurrentUserId();
        try {
            WalletDetailDTO wallet = walletService.getWalletDetail(walletId, userId);
            return ResponseEntity.ok(wallet);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
