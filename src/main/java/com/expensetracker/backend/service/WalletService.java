package com.expensetracker.backend.service;

import com.expensetracker.backend.dto.WalletDTO;
import com.expensetracker.backend.dto.WalletDetailDTO;
import com.expensetracker.backend.dto.WalletMemberDTO;
import com.expensetracker.backend.model.User;
import com.expensetracker.backend.model.Wallet;
import com.expensetracker.backend.model.WalletMember;
import com.expensetracker.backend.repository.UserRepository;
import com.expensetracker.backend.repository.WalletMemberRepository;
import com.expensetracker.backend.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletMemberRepository walletMemberRepository;
    private final UserRepository userRepository;

    @Autowired
    public WalletService(WalletRepository walletRepository, 
                        WalletMemberRepository walletMemberRepository,
                        UserRepository userRepository) {
        this.walletRepository = walletRepository;
        this.walletMemberRepository = walletMemberRepository;
        this.userRepository = userRepository;
    }

    public WalletDTO createWallet(String name, Wallet.WalletType type, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Wallet wallet = Wallet.builder()
                .name(name)
                .type(type)
                .build();

        Wallet savedWallet = walletRepository.save(wallet);

        WalletMember ownerMember = WalletMember.builder()
                .wallet(savedWallet)
                .user(user)
                .role(WalletMember.MemberRole.owner)
                .build();

        walletMemberRepository.save(ownerMember);

        return convertToDTO(savedWallet, 1);
    }

    public WalletMemberDTO inviteMember(UUID walletId, String username, UUID inviterId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        WalletMember inviterMember = walletMemberRepository.findByWalletIdAndUserId(walletId, inviterId)
                .orElseThrow(() -> new RuntimeException("You are not a member of this wallet"));

        if (inviterMember.getRole() != WalletMember.MemberRole.owner) {
            throw new SecurityException("Only wallet owner can invite members");
        }

        User userToInvite = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (walletMemberRepository.existsByWalletIdAndUserId(walletId, userToInvite.getId())) {
            throw new RuntimeException("User is already a member of this wallet");
        }

        WalletMember newMember = WalletMember.builder()
                .wallet(wallet)
                .user(userToInvite)
                .role(WalletMember.MemberRole.member)
                .build();

        WalletMember savedMember = walletMemberRepository.save(newMember);

        return convertMemberToDTO(savedMember);
    }

    public List<WalletDTO> getUserWallets(UUID userId) {
        List<Wallet> wallets = walletRepository.findAllByUserId(userId);
        return wallets.stream()
                .map(wallet -> {
                    int memberCount = walletMemberRepository.findAllByWalletId(wallet.getId()).size();
                    return convertToDTO(wallet, memberCount);
                })
                .collect(Collectors.toList());
    }

    public WalletDetailDTO getWalletDetail(UUID walletId, UUID userId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (!walletMemberRepository.existsByWalletIdAndUserId(walletId, userId)) {
            throw new SecurityException("You are not a member of this wallet");
        }

        List<WalletMember> members = walletMemberRepository.findAllByWalletId(walletId);
        List<WalletMemberDTO> memberDTOs = members.stream()
                .map(this::convertMemberToDTO)
                .collect(Collectors.toList());

        return WalletDetailDTO.builder()
                .id(wallet.getId())
                .name(wallet.getName())
                .type(wallet.getType())
                .members(memberDTOs)
                .build();
    }

    private WalletDTO convertToDTO(Wallet wallet, int memberCount) {
        return WalletDTO.builder()
                .id(wallet.getId())
                .name(wallet.getName())
                .type(wallet.getType())
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .memberCount(memberCount)
                .build();
    }

    private WalletMemberDTO convertMemberToDTO(WalletMember member) {
        return WalletMemberDTO.builder()
                .id(member.getId())
                .userId(member.getUser().getId())
                .username(member.getUser().getUsername())
                .name(member.getUser().getName())
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}
