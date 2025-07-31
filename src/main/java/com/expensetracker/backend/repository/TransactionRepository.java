package com.expensetracker.backend.repository;

import com.expensetracker.backend.model.Transaction;
import com.expensetracker.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    // Tìm tất cả giao dịch của một người dùng cụ thể
    List<Transaction> findByUser(User user);

    // Tìm giao dịch của một người dùng trong một khoảng thời gian
    List<Transaction> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);

    // Tìm giao dịch của một người dùng theo category (chuỗi)
    List<Transaction> findByUserAndCategory(User user, String category);

    // Tìm giao dịch của một người dùng theo loại (income/expense)
    List<Transaction> findByUserAndType(User user, Transaction.TransactionType type);

    List<Transaction> findByUser(User user, Pageable pageable);

    List<Transaction> findByUserId(UUID userId);

    List<Transaction> findByUserId(UUID userId, Pageable pageable);
}