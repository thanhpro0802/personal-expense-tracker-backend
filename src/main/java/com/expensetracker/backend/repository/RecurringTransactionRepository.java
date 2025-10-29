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

    List<RecurringTransaction> findByWallet_Id(UUID walletId);

    Optional<RecurringTransaction> findByIdAndWallet_Id(UUID id, UUID walletId);

    // Tối ưu hóa cho việc xóa
    boolean existsByIdAndWallet_Id(UUID id, UUID walletId);

    // Phương thức cốt lõi cho bộ lập lịch
    List<RecurringTransaction> findByIsActiveTrueAndNextExecutionDateLessThanEqual(LocalDate date);
}