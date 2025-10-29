package com.expensetracker.backend.service;

import com.expensetracker.backend.model.RecurringTransaction;
import com.expensetracker.backend.model.Transaction;
import com.expensetracker.backend.repository.RecurringTransactionRepository;
import com.expensetracker.backend.repository.TransactionRepository; // Import TransactionRepository
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
    private RecurringTransactionRepository recurringRepository;

    @Autowired
    private TransactionRepository transactionRepository; // Sử dụng TransactionRepository để lưu

    /**
     * Chạy mỗi ngày vào lúc 2 giờ sáng theo giờ server.
     * CRON: Giây Phút Giờ Ngày Tháng NgàyTrongTuần
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void processRecurringTransactions() {
        LocalDate today = LocalDate.now();
        logger.info("Running RecurringTransactionScheduler for date: {}", today);

        // 1. Lấy tất cả các tác vụ đang hoạt động và đã đến hạn hoặc quá hạn
        List<RecurringTransaction> tasksToRun = recurringRepository
                .findByIsActiveTrueAndNextExecutionDateLessThanEqual(today);

        logger.info("Found {} recurring tasks to process.", tasksToRun.size());

        for (RecurringTransaction task : tasksToRun) {
            // --- BỘ NÃO CỦA HỆ THỐNG: VÒNG LẶP "BẮT KỊP" (CATCH-UP LOOP) ---
            while (task.isActive() && !task.getNextExecutionDate().isAfter(today)) {
                try {
                    logger.info("Processing task ID: {} for execution date: {}", task.getId(), task.getNextExecutionDate());

                    // 2. Tạo giao dịch thực tế dựa trên thông tin của tác vụ
                    Transaction newTransaction = Transaction.builder()
                            .wallet(task.getWallet())
                            .title(task.getTitle())
                            .amount(task.getAmount())
                            .category(task.getCategory())
                            .type(task.getType())
                            .date(task.getNextExecutionDate()) // Ngày giao dịch là ngày lẽ ra nó phải được thực thi
                            .build();

                    transactionRepository.save(newTransaction); // Lưu giao dịch mới

                    // 3. Tính toán ngày thực thi tiếp theo
                    LocalDate newNextExecutionDate = calculateNextExecutionDate(
                            task.getNextExecutionDate(),
                            task.getFrequency()
                    );

                    // 4. Cập nhật tác vụ định kỳ
                    // Nếu có ngày kết thúc và ngày tiếp theo vượt qua ngày kết thúc -> vô hiệu hóa
                    if (task.getEndDate() != null && newNextExecutionDate.isAfter(task.getEndDate())) {
                        task.setActive(false);
                        logger.info("Deactivating recurring task ID: {}. End date reached.", task.getId());
                    } else {
                        task.setNextExecutionDate(newNextExecutionDate);
                    }

                } catch (Exception e) {
                    logger.error("Failed to process recurring task ID: {}. It will be retried next time.", task.getId(), e);
                    // Dừng xử lý tác vụ này để tránh vòng lặp vô hạn nếu có lỗi nghiêm trọng
                    break;
                }
            }
            // 5. Lưu lại trạng thái cuối cùng của tác vụ (nextExecutionDate mới hoặc isActive=false)
            recurringRepository.save(task);
        }
        logger.info("Finished processing recurring tasks.");
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