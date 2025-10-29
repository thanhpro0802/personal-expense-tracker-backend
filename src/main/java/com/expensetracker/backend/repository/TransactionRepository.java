package com.expensetracker.backend.repository;

import com.expensetracker.backend.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.math.BigDecimal;
import java.util.Map;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

    // --- CÁC PHƯƠNG THỨC MỚI CHO DASHBOARD THEO THÁNG ---

    @Query("SELECT COALESCE(SUM(t.amount), 0.0) FROM Transaction t WHERE t.wallet.id = :walletId AND t.type = :type AND EXTRACT(month FROM t.date) = :month AND EXTRACT(year FROM t.date) = :year")
    BigDecimal sumAmountByTypeAndMonthAndYear(@Param("walletId") UUID walletId, @Param("type") Transaction.TransactionType type, @Param("month") int month, @Param("year") int year);

    @Query("SELECT new map(t.category as category, SUM(t.amount) as amount) " +
            "FROM Transaction t " +
            "WHERE t.wallet.id = :walletId AND t.type = 'expense' AND EXTRACT(month FROM t.date) = :month AND EXTRACT(year FROM t.date) = :year " +
            "GROUP BY t.category ORDER BY SUM(t.amount) DESC")
    List<Map<String, Object>> findExpenseByCategoryAndMonthAndYear(@Param("walletId") UUID walletId, @Param("month") int month, @Param("year") int year);

    // --- SỬA LỖI Ở ĐÂY: Đảm bảo câu truy vấn không lọc theo loại giao dịch ---
    @Query("SELECT t FROM Transaction t WHERE t.wallet.id = :walletId AND EXTRACT(month FROM t.date) = :month AND EXTRACT(year FROM t.date) = :year ORDER BY t.date DESC, t.createdAt DESC")
    List<Transaction> findRecentTransactionsByMonthAndYear(@Param("walletId") UUID walletId, @Param("month") int month, @Param("year") int year, Pageable pageable);


    // --- CÁC PHƯƠNG THỨC CŨ HƠN (có thể đã tồn tại) ---
    @Query("SELECT COALESCE(SUM(t.amount), 0.0) FROM Transaction t WHERE t.wallet.id = :walletId AND t.type = :type")
    BigDecimal sumAmountByTypeAndWalletId(@Param("walletId") UUID walletId, @Param("type") Transaction.TransactionType type);

    @Query("SELECT new map(t.category as category, SUM(t.amount) as amount) " +
            "FROM Transaction t " +
            "WHERE t.wallet.id = :walletId AND t.type = 'expense' " +
            "GROUP BY t.category " +
            "ORDER BY SUM(t.amount) DESC")
    List<Map<String, Object>> findExpenseByCategory(@Param("walletId") UUID walletId);

    @Query("SELECT new map(FUNCTION('TO_CHAR', t.date, 'YYYY-MM') as month, " +
            "SUM(CASE WHEN t.type = 'income' THEN t.amount ELSE 0 END) as income, " +
            "SUM(CASE WHEN t.type = 'expense' THEN t.amount ELSE 0 END) as expenses) " +
            "FROM Transaction t " +
            "WHERE t.wallet.id = :walletId " +
            "GROUP BY FUNCTION('TO_CHAR', t.date, 'YYYY-MM') " +
            "ORDER BY FUNCTION('TO_CHAR', t.date, 'YYYY-MM') ASC")
    List<Map<String, Object>> findMonthlySummary(@Param("walletId") UUID walletId);

    @Query("SELECT new map(t.category as category, SUM(t.amount) as spent) " +
            "FROM Transaction t " +
            "WHERE t.wallet.id = :walletId AND t.type = 'expense' " +
            "AND EXTRACT(YEAR FROM t.date) = :year " +
            "AND EXTRACT(MONTH FROM t.date) = :month " +
            "GROUP BY t.category")
    List<Map<String, Object>> findExpenseSumByCategoryAndMonthYear(
            @Param("walletId") UUID walletId,
            @Param("month") int month,
            @Param("year") int year
    );

    Optional<Transaction> findByIdAndWallet_Id(UUID id, UUID walletId);
    Page<Transaction> findByWallet_Id(UUID walletId, Pageable pageable);
}