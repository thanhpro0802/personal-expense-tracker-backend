package com.expensetracker.backend.service;

import com.expensetracker.backend.model.Category;
import com.expensetracker.backend.model.Transaction;
import com.expensetracker.backend.model.User;
import com.expensetracker.backend.repository.CategoryRepository;
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
    private final CategoryRepository categoryRepository;

    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository, CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
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
        return transactionRepository.findByUserAndTransactionDateBetween(user, startDate, endDate);
    }

    @Transactional
    public Transaction createTransaction(Transaction transaction) {
        // Đảm bảo User tồn tại
        User user = userRepository.findById(transaction.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + transaction.getUser().getId()));
        transaction.setUser(user);

        // Đảm bảo Category tồn tại và thuộc về user hoặc là default category
        Category category = categoryRepository.findById(transaction.getCategory().getId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + transaction.getCategory().getId()));

        if (category.getUser() != null && !category.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Category does not belong to this user or is not a default category.");
        }
        transaction.setCategory(category);

        // Đảm bảo type của transaction khớp với type của category
        if (transaction.getType() != category.getType()) {
            throw new RuntimeException("Transaction type must match category type. Transaction: " + transaction.getType() + ", Category: " + category.getType());
        }

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction updateTransaction(UUID id, Transaction transactionDetails, UUID userId) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));

        // Đảm bảo người dùng sở hữu giao dịch này
        if (!transaction.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access Denied: You do not have permission to update this transaction.");
        }

        // Cập nhật các trường
        transaction.setAmount(transactionDetails.getAmount());
        transaction.setTransactionDate(transactionDetails.getTransactionDate());
        transaction.setDescription(transactionDetails.getDescription());
        transaction.setPaymentMethod(transactionDetails.getPaymentMethod());

        // Cập nhật category và type (cần kiểm tra tương tự như khi tạo)
        if (!transaction.getCategory().getId().equals(transactionDetails.getCategory().getId())) {
            Category newCategory = categoryRepository.findById(transactionDetails.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("New Category not found with id: " + transactionDetails.getCategory().getId()));
            if (newCategory.getUser() != null && !newCategory.getUser().getId().equals(userId)) {
                throw new RuntimeException("New category does not belong to this user or is not a default category.");
            }
            transaction.setCategory(newCategory);
        }

        // Đảm bảo type của transaction khớp với type của category mới
        if (transactionDetails.getType() != transaction.getCategory().getType()) {
            throw new RuntimeException("Transaction type must match category type.");
        }
        transaction.setType(transactionDetails.getType());


        return transactionRepository.save(transaction);
    }

    @Transactional
    public void deleteTransaction(UUID id, UUID userId) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));

        // Đảm bảo người dùng sở hữu giao dịch này
        if (!transaction.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access Denied: You do not have permission to delete this transaction.");
        }

        transactionRepository.delete(transaction);
    }
}