package com.expensetracker.backend.service;

import com.expensetracker.backend.model.Transaction;
import com.expensetracker.backend.model.User;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Page<Transaction> getFilteredTransactions(UUID userId, String type, String category, String search,
                                                     LocalDate dateFrom, LocalDate dateTo,
                                                     int page, int size, String[] sort) {

        // --- SỬA LỖI DEPRECATION TẠI ĐÂY ---
        // Bắt đầu trực tiếp với Specification đầu tiên, không cần dùng Specification.where()
        Specification<Transaction> spec = TransactionSpecifications.withUserId(userId);

        // Các điều kiện sau được nối vào bằng .and() như bình thường
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

        // Xử lý thông tin sắp xếp (sorting)
        List<Sort.Order> orders = new ArrayList<>();
        if (sort[0].contains(",")) {
            for (String sortOrder : sort) {
                String[] _sort = sortOrder.split(",");
                orders.add(new Sort.Order(Sort.Direction.fromString(_sort[1]), _sort[0]));
            }
        } else {
            orders.add(new Sort.Order(Sort.Direction.fromString(sort[1]), sort[0]));
        }

        // Tạo đối tượng Pageable
        Pageable pageable = PageRequest.of(page, size, Sort.by(orders));

        // Thực thi truy vấn
        return transactionRepository.findAll(spec, pageable);
    }

    public Transaction createTransaction(Transaction transaction, UUID userId) {
        User userReference = new User();
        userReference.setId(userId);
        transaction.setUser(userReference);
        return transactionRepository.save(transaction);
    }

    public Transaction updateTransaction(UUID transactionId, Transaction transactionDetails, UUID userId) {
        Transaction existingTransaction = transactionRepository.findById(transactionId)
                .filter(t -> t.getUser().getId().equals(userId))
                .orElseThrow(() -> new SecurityException("Transaction not found or access denied"));

        existingTransaction.setTitle(transactionDetails.getTitle());
        existingTransaction.setAmount(transactionDetails.getAmount());
        existingTransaction.setDate(transactionDetails.getDate());
        existingTransaction.setCategory(transactionDetails.getCategory());
        existingTransaction.setType(transactionDetails.getType());

        return transactionRepository.save(existingTransaction);
    }

    public void deleteTransaction(UUID transactionId, UUID userId) {
        if (!transactionRepository.existsById(transactionId)) {
            throw new RuntimeException("Transaction not found");
        }
        transactionRepository.findById(transactionId)
                .filter(t -> t.getUser().getId().equals(userId))
                .orElseThrow(() -> new SecurityException("Access denied to delete this transaction"));

        transactionRepository.deleteById(transactionId);
    }
}