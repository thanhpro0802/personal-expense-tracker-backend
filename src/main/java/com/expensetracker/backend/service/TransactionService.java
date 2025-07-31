package com.expensetracker.backend.service;

import com.expensetracker.backend.model.Transaction;
import com.expensetracker.backend.model.User;
import com.expensetracker.backend.repository.TransactionRepository;
import com.expensetracker.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Transaction> getTransactionById(UUID id) {
        return transactionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getUserTransactions(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return transactionRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getUserTransactionsBetweenDates(UUID userId, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return transactionRepository.findByUserAndDateBetween(user, startDate, endDate);
    }

    @Transactional
    public Transaction createTransaction(Transaction transaction) {
        // Kiểm tra user tồn tại
        User user = userRepository.findById(transaction.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + transaction.getUser().getId()));

        transaction.setUser(user);

        // Kiểm tra giá trị type hợp lệ (income hoặc expense)
        if (transaction.getType() == null) {
            throw new RuntimeException("Transaction type is required");
        }

        // category chỉ là String nên không kiểm tra thêm

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction updateTransaction(UUID id, Transaction transactionDetails, UUID userId) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));

        if (!transaction.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access Denied: You do not have permission to update this transaction.");
        }

        transaction.setTitle(transactionDetails.getTitle());
        transaction.setAmount(transactionDetails.getAmount());
        transaction.setDate(transactionDetails.getDate());
        transaction.setCategory(transactionDetails.getCategory());
        transaction.setType(transactionDetails.getType());

        return transactionRepository.save(transaction);
    }

    @Transactional
    public void deleteTransaction(UUID id, UUID userId) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));

        if (!transaction.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access Denied: You do not have permission to delete this transaction.");
        }

        transactionRepository.delete(transaction);
    }
}
