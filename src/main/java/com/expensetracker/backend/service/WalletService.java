package com.expensetracker.backend.service;

import com.expensetracker.backend.model.User;
import com.expensetracker.backend.model.Wallet;
import com.expensetracker.backend.repository.UserRepository;
import com.expensetracker.backend.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    @Autowired
    public WalletService(WalletRepository walletRepository, UserRepository userRepository) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
    }

    /**
     * Lấy tất cả ví mà người dùng là owner hoặc member
     */
    public List<Wallet> getWalletsForUser(UUID userId) {
        return walletRepository.findAllWalletsByUserId(userId);
    }

    /**
     * Tạo ví mới và tự động thêm owner vào danh sách members
     */
    public Wallet createWallet(String name, UUID ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Wallet wallet = Wallet.builder()
                .name(name)
                .owner(owner)
                .build();

        // Tự động thêm owner vào danh sách members
        wallet.getMembers().add(owner);

        return walletRepository.save(wallet);
    }

    /**
     * Mời người dùng vào ví
     */
    public Wallet inviteUserToWallet(UUID walletId, String inviteeEmail, UUID ownerId) {
        Wallet wallet = walletRepository.findByIdWithMembers(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        // Kiểm tra quyền: chỉ owner mới có thể mời
        if (!wallet.getOwner().getId().equals(ownerId)) {
            throw new SecurityException("Only the wallet owner can invite members");
        }

        User invitee = userRepository.findByEmail(inviteeEmail)
                .orElseThrow(() -> new IllegalArgumentException("User with email " + inviteeEmail + " not found"));

        // Thêm vào danh sách members
        wallet.getMembers().add(invitee);

        return walletRepository.save(wallet);
    }

    /**
     * Kiểm tra xem người dùng có phải là member của ví không
     */
    public boolean isUserMemberOfWallet(UUID walletId, UUID userId) {
        Wallet wallet = walletRepository.findByIdWithMembers(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        return wallet.getMembers().stream()
                .anyMatch(member -> member.getId().equals(userId)) ||
                wallet.getOwner().getId().equals(userId);
    }

    /**
     * Lấy ví theo ID
     */
    public Wallet getWalletById(UUID walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
    }
}
