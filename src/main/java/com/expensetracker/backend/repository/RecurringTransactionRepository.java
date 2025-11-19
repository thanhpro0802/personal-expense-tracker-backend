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

    List<RecurringTransaction> findByUser_Id(UUID userId);

    Optional<RecurringTransaction> findByIdAndUser_Id(UUID id, UUID userId);

    // Tối ưu hóa cho việc xóa
    boolean existsByIdAndUser_Id(UUID id, UUID userId);

    // Phương thức cốt lõi cho bộ lập lịch
    List<RecurringTransaction> findByIsActiveTrueAndNextExecutionDateLessThanEqual(LocalDate date);
}