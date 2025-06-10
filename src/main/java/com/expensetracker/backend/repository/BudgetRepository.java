package com.expensetracker.backend.repository;

import com.expensetracker.backend.model.Budget;
import com.expensetracker.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, UUID> {
    // Tìm tất cả ngân sách của một người dùng cụ thể
    List<Budget> findByUser(User user);

    // Tìm ngân sách của một người dùng trong một khoảng thời gian (start_date <= endDate AND end_date >= startDate)
    List<Budget> findByUserAndStartDateLessThanEqualAndEndDateGreaterThanEqual(User user, LocalDate endDate, LocalDate startDate);

    // Tìm ngân sách theo user và category
    Optional<Budget> findByUserAndCategory(User user, com.expensetracker.backend.model.Category category);

    // Tìm ngân sách của một người dùng trong một khoảng thời gian cụ thể cho một danh mục cụ thể
    List<Budget> findByUserAndCategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(User user, com.expensetracker.backend.model.Category category, LocalDate endDate, LocalDate startDate);
}