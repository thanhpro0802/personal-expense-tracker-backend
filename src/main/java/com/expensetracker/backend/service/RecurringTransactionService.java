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

        // Đặt ngày thực thi tiếp theo là ngày bắt đầu
        rt.setNextExecutionDate(rt.getStartDate());
        rt.setActive(true);

        return recurringRepository.save(rt);
    }

    public List<RecurringTransaction> getAllForUser(UUID userId) {
        return recurringRepository.findByUser_Id(userId);
    }

    public RecurringTransaction updateRecurringTransaction(UUID id, RecurringTransaction details, UUID userId) {
        RecurringTransaction existing = recurringRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new SecurityException("Recurring transaction not found or access denied"));

        // Cập nhật các trường
        existing.setTitle(details.getTitle());
        existing.setAmount(details.getAmount());
        existing.setCategory(details.getCategory());
        existing.setType(details.getType());
        existing.setFrequency(details.getFrequency());
        existing.setStartDate(details.getStartDate());
        existing.setEndDate(details.getEndDate());
        existing.setActive(details.isActive());

        // Nếu người dùng cập nhật ngày bắt đầu, ta cũng nên cập nhật ngày thực thi tiếp theo
        if (details.getNextExecutionDate() != null) {
            existing.setNextExecutionDate(details.getNextExecutionDate());
        } else {
            existing.setNextExecutionDate(details.getStartDate());
        }


        return recurringRepository.save(existing);
    }

    public void deleteRecurringTransaction(UUID id, UUID userId) {
        RecurringTransaction existing = recurringRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new SecurityException("Recurring transaction not found or access denied"));

        recurringRepository.delete(existing);
    }
}