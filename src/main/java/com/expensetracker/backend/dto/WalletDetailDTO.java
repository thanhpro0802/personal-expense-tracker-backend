package com.expensetracker.backend.dto;

import com.expensetracker.backend.model.Wallet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletDetailDTO {
    private UUID id;
    private String name;
    private Wallet.WalletType type;
    private List<WalletMemberDTO> members;
}
