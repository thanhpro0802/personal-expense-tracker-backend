package com.expensetracker.backend.repository;

import com.expensetracker.backend.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    // Tìm tất cả ngân sách của người dùng cho một tháng/năm cụ thể
    List<Budget> findByUser_IdAndMonthAndYear(UUID userId, int month, int year);

    // Dùng để kiểm tra khi tạo/cập nhật
    Optional<Budget> findByUser_IdAndCategoryAndMonthAndYear(UUID userId, String category, int month, int year);

    // Dùng để xóa (kiểm tra quyền sở hữu)
    Optional<Budget> findByIdAndUser_Id(UUID id, UUID userId);
}