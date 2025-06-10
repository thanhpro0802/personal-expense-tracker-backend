package com.expensetracker.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal; // Dùng cho số tiền để tránh lỗi làm tròn
import java.time.LocalDate; // Chỉ lưu ngày tháng
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // Giao dịch luôn thuộc về một người dùng
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false) // Giao dịch luôn thuộc về một danh mục
    private Category category;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount; // Sử dụng BigDecimal cho tiền tệ để tránh lỗi làm tròn

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate; // Lưu trữ chỉ ngày (YYYY-MM-DD)

    @Column(name = "description", length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10) // INCOME hoặc EXPENSE
    private Category.CategoryType type; // Tái sử dụng enum CategoryType từ lớp Category

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}