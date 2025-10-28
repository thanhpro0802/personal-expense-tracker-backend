package com.expensetracker.backend.service;

import com.expensetracker.backend.model.RecurringTransaction;
import com.expensetracker.backend.model.Transaction;
import com.expensetracker.backend.repository.RecurringTransactionRepository; // Import repository
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class RecurringTransactionScheduler {

    private static final Logger logger = LoggerFactory.getLogger(RecurringTransactionScheduler.class);

    @Autowired
    private RecurringTransactionRepository recurringRepository; // Đã inject repository

    @Autowired
    private TransactionService transactionService;

    /**
     * Chạy mỗi ngày vào 5:00 AM.
     */
    @Scheduled(cron = "0 0 5 * * ?")
    @Transactional
    public void processRecurringTransactions() {
        LocalDate today = LocalDate.now();
        logger.info("Running RecurringTransactionScheduler at {}", today);

        // ✨ SỬA LỖI TẠI ĐÂY: Gọi tên phương thức mới ✨
        List<RecurringTransaction> tasksToRun = recurringRepository
                .findByIsActiveTrueAndNextExecutionDateLessThanEqual(today); // <-- Đổi tên ở đây

        logger.info("Found {} recurring tasks to process.", tasksToRun.size());

        for (RecurringTransaction task : tasksToRun) {
            try {
                // Tạo giao dịch thực tế
                Transaction newTransaction = Transaction.builder()
                        .user(task.getUser())
                        .title(task.getTitle())
                        .amount(task.getAmount())
                        .category(task.getCategory())
                        .type(task.getType())
                        .date(task.getNextExecutionDate())
                        .build();

                transactionService.createTransaction(newTransaction, task.getUser().getId());

                // Tính toán ngày thực thi tiếp theo
                LocalDate newNextExecutionDate = calculateNextExecutionDate(
                        task.getNextExecutionDate(),
                        task.getFrequency()
                );

                // Cập nhật tác vụ định kỳ
                if (task.getEndDate() != null && newNextExecutionDate.isAfter(task.getEndDate())) {
                    task.setActive(false);
                    logger.info("Deactivating recurring task ID: {}", task.getId());
                } else {
                    task.setNextExecutionDate(newNextExecutionDate);
                }

                recurringRepository.save(task);
                logger.info("Processed and updated next execution date for task ID: {}", task.getId());

            } catch (Exception e) {
                logger.error("Failed to process recurring task ID: {}", task.getId(), e);
            }
        }
    }

    private LocalDate calculateNextExecutionDate(LocalDate current, RecurringTransaction.Frequency freq) {
        switch (freq) {
            case DAILY:
                return current.plusDays(1);
            case WEEKLY:
                return current.plusWeeks(1);
            case MONTHLY:
                return current.plusMonths(1);
            case YEARLY:
                return current.plusYears(1);
            default:
                throw new IllegalArgumentException("Unknown frequency: " + freq);
        }
    }
}