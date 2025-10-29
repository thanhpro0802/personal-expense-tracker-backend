package com.expensetracker.backend.service;

import com.expensetracker.backend.model.RecurringTransaction;
import com.expensetracker.backend.model.User;
import com.expensetracker.backend.repository.RecurringTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RecurringTransactionService {

    @Autowired
    private RecurringTransactionRepository recurringRepository;

    public RecurringTransaction createRecurringTransaction(RecurringTransaction rt, UUID userId) {
        User userRef = new User();
        userRef.setId(userId);
        rt.setUser(userRef);

        rt.setNextExecutionDate(rt.getStartDate());
        rt.setActive(true);

        return recurringRepository.save(rt);
    }

    @Transactional(readOnly = true)
    public List<RecurringTransaction> getAllForUser(UUID userId) {
        return recurringRepository.findByUser_Id(userId);
    }

    public RecurringTransaction updateRecurringTransaction(UUID id, RecurringTransaction details, UUID userId) {
        RecurringTransaction existing = recurringRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new SecurityException("Recurring transaction not found or access denied"));

        // --- SỬA LỖI LOGIC QUAN TRỌNG NHẤT Ở ĐÂY ---
        // Chỉ cập nhật các trường nếu chúng được cung cấp trong yêu cầu (không phải null)

        if (details.getTitle() != null) {
            existing.setTitle(details.getTitle());
        }
        if (details.getAmount() != null) {
            existing.setAmount(details.getAmount());
        }
        if (details.getCategory() != null) {
            existing.setCategory(details.getCategory());
        }
        if (details.getType() != null) {
            existing.setType(details.getType());
        }
        if (details.getFrequency() != null) {
            existing.setFrequency(details.getFrequency());
        }
        if (details.getStartDate() != null) {
            existing.setStartDate(details.getStartDate());
            // Khi ngày bắt đầu thay đổi, reset lại ngày thực thi tiếp theo
            existing.setNextExecutionDate(details.getStartDate());
        }

        // Luôn cho phép cập nhật endDate thành null
        existing.setEndDate(details.getEndDate());

        // Cập nhật trạng thái active
        existing.setActive(details.isActive());

        return recurringRepository.save(existing);
    }

    public void deleteRecurringTransaction(UUID id, UUID userId) {
        if (!recurringRepository.existsByIdAndUser_Id(id, userId)) {
            throw new SecurityException("Recurring transaction not found or access denied");
        }
        recurringRepository.deleteById(id);
    }
}