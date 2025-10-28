package com.expensetracker.backend.repository;

import com.expensetracker.backend.model.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, UUID> {

    // Lấy tất cả các giao dịch định kỳ của một người dùng
    List<RecurringTransaction> findByUser_Id(UUID userId);

    // Lấy một giao dịch cụ thể để cập nhật/xóa (kiểm tra quyền sở hữu)
    Optional<RecurringTransaction> findByIdAndUser_Id(UUID id, UUID userId);

    // QUAN TRỌNG: Lấy tất cả các giao dịch đang hoạt động
    // có ngày thực thi tiếp theo là hôm nay hoặc sớm hơn (để chạy)
    // ✨ SỬA LỖI TẠI ĐÂY: Đổi "OnOrBefore" thành "LessThanEqual" ✨
    List<RecurringTransaction> findByIsActiveTrueAndNextExecutionDateLessThanEqual(LocalDate date);
}