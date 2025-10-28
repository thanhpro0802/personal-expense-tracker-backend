package com.expensetracker.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetDTO {
    private UUID id;
    private String category;
    private BigDecimal amount; // Giới hạn ngân sách
    private BigDecimal spentAmount; // Đã chi tiêu
    private BigDecimal remainingAmount; // Còn lại
    private int month;
    private int year;
}