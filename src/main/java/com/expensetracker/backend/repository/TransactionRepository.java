package com.expensetracker.backend.repository;

import com.expensetracker.backend.model.Transaction;
import com.expensetracker.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    // Tìm tất cả giao dịch của một người dùng cụ thể
    List<Transaction> findByUser(User user);

    // Tìm giao dịch của một người dùng trong một khoảng thời gian
    List<Transaction> findByUserAndTransactionDateBetween(User user, LocalDate startDate, LocalDate endDate);

    // Tìm giao dịch của một người dùng theo danh mục
    List<Transaction> findByUserAndCategory(User user, com.expensetracker.backend.model.Category category);

    // Tìm giao dịch của một người dùng theo loại (INCOME/EXPENSE)
    List<Transaction> findByUserAndType(User user, com.expensetracker.backend.model.Category.CategoryType type);
}