package com.expensetracker.backend.dto;

import com.expensetracker.backend.model.WalletMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletMemberDTO {
    private UUID id;
    private UUID userId;
    private String username;
    private String name;
    private WalletMember.MemberRole role;
    private LocalDateTime joinedAt;
}
