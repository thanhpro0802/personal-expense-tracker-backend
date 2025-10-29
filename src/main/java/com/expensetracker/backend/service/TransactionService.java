package com.expensetracker.backend.service;

import com.expensetracker.backend.model.Budget;
import com.expensetracker.backend.model.Transaction;
import com.expensetracker.backend.model.User;
import com.expensetracker.backend.repository.BudgetRepository;
import com.expensetracker.backend.repository.TransactionRepository;
import com.expensetracker.backend.service.specifications.TransactionSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository, BudgetRepository budgetRepository) {
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
    }

    public Page<Transaction> getFilteredTransactions(UUID userId, String type, String category, String search,
                                                     LocalDate dateFrom, LocalDate dateTo,
                                                     int page, int size, String[] sort) {

        Specification<Transaction> spec = TransactionSpecifications.withUserId(userId);

        if (type != null && !type.isEmpty() && !type.equalsIgnoreCase("all")) {
            spec = spec.and(TransactionSpecifications.withType(type));
        }
        if (category != null && !category.isEmpty()) {
            spec = spec.and(TransactionSpecifications.withCategory(category));
        }
        if (search != null && !search.isEmpty()) {
            spec = spec.and(TransactionSpecifications.withSearchText(search));
        }
        if (dateFrom != null) {
            spec = spec.and(TransactionSpecifications.withDateFrom(dateFrom));
        }
        if (dateTo != null) {
            spec = spec.and(TransactionSpecifications.withDateTo(dateTo));
        }

        List<Sort.Order> orders = new ArrayList<>();
        if (sort[0].contains(",")) {
            for (String sortOrder : sort) {
                String[] _sort = sortOrder.split(",");
                orders.add(new Sort.Order(Sort.Direction.fromString(_sort[1]), _sort[0]));
            }
        } else {
            orders.add(new Sort.Order(Sort.Direction.fromString(sort[1]), sort[0]));
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(orders));
        return transactionRepository.findAll(spec, pageable);
    }

    public Transaction createTransaction(Transaction transaction, UUID userId) {
        User userReference = new User();
        userReference.setId(userId);
        transaction.setUser(userReference);
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Logic cập nhật budget - Sửa lại thành chữ thường
        if (savedTransaction.getType() == Transaction.TransactionType.expense) {
            updateBudgetSpentAmount(userId, savedTransaction.getCategory(),
                    savedTransaction.getDate().getYear(), savedTransaction.getDate().getMonthValue(),
                    savedTransaction.getAmount());
        }

        return savedTransaction;
    }

    public Transaction updateTransaction(UUID transactionId, Transaction transactionDetails, UUID userId) {
        Transaction existingTransaction = transactionRepository.findById(transactionId)
                .filter(t -> t.getUser().getId().equals(userId))
                .orElseThrow(() -> new SecurityException("Transaction not found or access denied"));

        Transaction oldTransaction = new Transaction();
        oldTransaction.setType(existingTransaction.getType());
        oldTransaction.setAmount(existingTransaction.getAmount());
        oldTransaction.setCategory(existingTransaction.getCategory());
        oldTransaction.setDate(existingTransaction.getDate());

        existingTransaction.setTitle(transactionDetails.getTitle());
        existingTransaction.setAmount(transactionDetails.getAmount());
        existingTransaction.setDate(transactionDetails.getDate());
        existingTransaction.setCategory(transactionDetails.getCategory());
        existingTransaction.setType(transactionDetails.getType());
        Transaction updatedTransaction = transactionRepository.save(existingTransaction);

        // Logic cập nhật budget - Sửa lại thành chữ thường
        if (oldTransaction.getType() == Transaction.TransactionType.expense) {
            updateBudgetSpentAmount(userId, oldTransaction.getCategory(),
                    oldTransaction.getDate().getYear(), oldTransaction.getDate().getMonthValue(),
                    oldTransaction.getAmount().negate());
        }

        if (updatedTransaction.getType() == Transaction.TransactionType.expense) {
            updateBudgetSpentAmount(userId, updatedTransaction.getCategory(),
                    updatedTransaction.getDate().getYear(), updatedTransaction.getDate().getMonthValue(),
                    updatedTransaction.getAmount());
        }

        return updatedTransaction;
    }

    public void deleteTransaction(UUID transactionId, UUID userId) {
        Transaction transactionToDelete = transactionRepository.findById(transactionId)
                .filter(t -> t.getUser().getId().equals(userId))
                .orElseThrow(() -> new SecurityException("Transaction not found or access denied to delete"));

        // Logic cập nhật budget - Sửa lại thành chữ thường
        if (transactionToDelete.getType() == Transaction.TransactionType.expense) {
            updateBudgetSpentAmount(userId, transactionToDelete.getCategory(),
                    transactionToDelete.getDate().getYear(), transactionToDelete.getDate().getMonthValue(),
                    transactionToDelete.getAmount().negate());
        }

        transactionRepository.delete(transactionToDelete);
    }

    private void updateBudgetSpentAmount(UUID userId, String category, int year, int month, BigDecimal amountChange) {
        Optional<Budget> budgetOpt = budgetRepository.findByUser_IdAndCategoryAndMonthAndYear(userId, category, month, year);

        budgetOpt.ifPresent(budget -> {
            BigDecimal newSpentAmount = budget.getSpentAmount().add(amountChange);
            budget.setSpentAmount(newSpentAmount);
            budgetRepository.save(budget);
        });
    }
}