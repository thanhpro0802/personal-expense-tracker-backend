package com.expensetracker.backend.repository;

import com.expensetracker.backend.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List; // Import List
import java.util.Optional;
import java.util.UUID;
import java.math.BigDecimal;
import java.util.Map;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

    // --- TRUY VẤN TỐI ƯU HÓA CHO DASHBOARD ---

    @Query("SELECT COALESCE(SUM(t.amount), 0.0) FROM Transaction t WHERE t.user.id = :userId AND t.type = :type")
    BigDecimal sumAmountByTypeAndUserId(@Param("userId") UUID userId, @Param("type") Transaction.TransactionType type);

    @Query("SELECT new map(t.category as category, SUM(t.amount) as amount) " +
            "FROM Transaction t " +
            "WHERE t.user.id = :userId AND t.type = 'expense' " +
            "GROUP BY t.category " +
            "ORDER BY SUM(t.amount) DESC")
    List<Map<String, Object>> findExpenseByCategory(@Param("userId") UUID userId);

    /**
     * --- SỬA LỖI "Cannot resolve symbol 'month'" TẠI ĐÂY ---
     * Thay thế `ORDER BY month` bằng cách lặp lại toàn bộ hàm.
     */
    @Query("SELECT new map(FUNCTION('TO_CHAR', t.date, 'YYYY-MM') as month, " +
            "SUM(CASE WHEN t.type = 'income' THEN t.amount ELSE 0 END) as income, " +
            "SUM(CASE WHEN t.type = 'expense' THEN t.amount ELSE 0 END) as expenses) " +
            "FROM Transaction t " +
            "WHERE t.user.id = :userId " +
            "GROUP BY FUNCTION('TO_CHAR', t.date, 'YYYY-MM') " +
            "ORDER BY FUNCTION('TO_CHAR', t.date, 'YYYY-MM') ASC")
    List<Map<String, Object>> findMonthlySummary(@Param("userId") UUID userId);
    

    // --- Phương thức để lấy dữ liệu cho DashboardService ---
    // Trả về một List, dùng để tính toán thống kê
    List<Transaction> findByUserId(UUID userId);

    // --- Phương thức để lấy dữ liệu phân trang ---
    // Trả về một Page, dùng cho API lấy danh sách giao dịch
    Page<Transaction> findByUserId(UUID userId, Pageable pageable);

    // --- Phương thức bảo mật để lấy giao dịch cụ thể ---
    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);

    List<Transaction> findByUser_Id(UUID userId);

    Page<Transaction> findByUser_Id(UUID userId, Pageable pageable);
}