package com.expensetracker.backend.service;

import com.expensetracker.backend.model.RecurringTransaction;
import com.expensetracker.backend.model.Wallet;
import com.expensetracker.backend.repository.RecurringTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class RecurringTransactionService {

    @Autowired
    private RecurringTransactionRepository recurringRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public RecurringTransaction createRecurringTransaction(RecurringTransaction rt, UUID walletId, UUID userId) {
        // Kiểm tra quyền truy cập
        if (!walletService.isUserMemberOfWallet(walletId, userId)) {
            throw new SecurityException("User is not a member of this wallet");
        }

        Wallet walletRef = new Wallet();
        walletRef.setId(walletId);
        rt.setWallet(walletRef);

        // Đặt ngày thực thi tiếp theo là ngày bắt đầu
        rt.setNextExecutionDate(rt.getStartDate());
        rt.setActive(true);

        RecurringTransaction saved = recurringRepository.save(rt);

        // Gửi thông báo WebSocket
        messagingTemplate.convertAndSend("/topic/wallet/" + walletId, 
                Map.of("message", "DATA_UPDATED", "type", "RECURRING_TRANSACTION_CREATED"));

        return saved;
    }

    public List<RecurringTransaction> getAllForWallet(UUID walletId, UUID userId) {
        // Kiểm tra quyền truy cập
        if (!walletService.isUserMemberOfWallet(walletId, userId)) {
            throw new SecurityException("User is not a member of this wallet");
        }

        return recurringRepository.findByWallet_Id(walletId);
    }

    public RecurringTransaction updateRecurringTransaction(UUID id, RecurringTransaction details, UUID walletId, UUID userId) {
        // Kiểm tra quyền truy cập
        if (!walletService.isUserMemberOfWallet(walletId, userId)) {
            throw new SecurityException("User is not a member of this wallet");
        }

        RecurringTransaction existing = recurringRepository.findByIdAndWallet_Id(id, walletId)
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

        RecurringTransaction updated = recurringRepository.save(existing);

        // Gửi thông báo WebSocket
        messagingTemplate.convertAndSend("/topic/wallet/" + walletId, 
                Map.of("message", "DATA_UPDATED", "type", "RECURRING_TRANSACTION_UPDATED"));

        return updated;
    }

    public void deleteRecurringTransaction(UUID id, UUID walletId, UUID userId) {
        // Kiểm tra quyền truy cập
        if (!walletService.isUserMemberOfWallet(walletId, userId)) {
            throw new SecurityException("User is not a member of this wallet");
        }

        RecurringTransaction existing = recurringRepository.findByIdAndWallet_Id(id, walletId)
                .orElseThrow(() -> new SecurityException("Recurring transaction not found or access denied"));

        recurringRepository.delete(existing);

        // Gửi thông báo WebSocket
        messagingTemplate.convertAndSend("/topic/wallet/" + walletId, 
                Map.of("message", "DATA_UPDATED", "type", "RECURRING_TRANSACTION_DELETED"));
    }
}