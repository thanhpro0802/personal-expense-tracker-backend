package com.expensetracker.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "budgets", uniqueConstraints = {
        // Đảm bảo mỗi người dùng chỉ có 1 ngân sách cho 1 danh mục trong 1 tháng/năm
        @UniqueConstraint(columnNames = {"user_id", "category", "month", "year"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount; // Số tiền ngân sách

    // --- TRƯỜNG MỚI ĐƯỢC THÊM ---
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal spentAmount = BigDecimal.ZERO; // Số tiền đã chi tiêu

    @Column(nullable = false)
    private int month; // 1-12

    @Column(nullable = false)
    private int year;

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