package com.expensetracker.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "recurring_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    // Thông tin giống như Transaction
    @Column(nullable = false)
    private String title;

    // --- THÊM TRƯỜNG BỊ THIẾU Ở ĐÂY ---
    @Column
    private String description; // Cho phép trường này có giá trị null

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false)
    private String category;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Transaction.TransactionType type;

    // Thông tin lập lịch
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Frequency frequency;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate nextExecutionDate; // Ngày quan trọng nhất để bộ lập lịch kiểm tra

    @Column
    private LocalDate endDate; // Có thể null (nếu là mãi mãi)

    @Builder.Default
    private boolean isActive = true;

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

    public enum Frequency {
        DAILY,
        WEEKLY,
        MONTHLY,
        YEARLY
    }
}