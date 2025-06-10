package com.expensetracker.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Quan hệ ManyToOne với User (user_id có thể NULL)
    @ManyToOne(fetch = FetchType.LAZY) // Lazy loading để tránh tải User không cần thiết
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = true) // user_id có thể NULL
    private User user; // Đối tượng User mà danh mục này thuộc về (nếu là danh mục tùy chỉnh)

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING) // Lưu trữ enum dưới dạng String trong DB
    @Column(name = "type", nullable = false, length = 10) // INCOME hoặc EXPENSE
    private CategoryType type;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "icon_name", length = 50)
    private String iconName; // Ví dụ: 'fas-utensils'

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

    // Enum cho Category Type
    public enum CategoryType {
        INCOME,
        EXPENSE
    }
}
