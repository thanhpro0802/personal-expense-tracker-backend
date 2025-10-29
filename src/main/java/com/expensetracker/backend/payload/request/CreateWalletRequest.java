package com.expensetracker.backend.payload.request;

import com.expensetracker.backend.model.Wallet;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateWalletRequest {
    
    @NotBlank(message = "Wallet name is required")
    private String name;
    
    @NotNull(message = "Wallet type is required")
    private Wallet.WalletType type;
}
