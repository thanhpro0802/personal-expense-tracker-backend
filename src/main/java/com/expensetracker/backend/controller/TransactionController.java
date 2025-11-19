package com.expensetracker.backend.controller;

import com.expensetracker.backend.model.Transaction;
import com.expensetracker.backend.security.services.UserDetailsImpl; // Thêm import này
import com.expensetracker.backend.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate; // Thêm import cho các tham số filter
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * --- SỬA LỖI QUAN TRỌNG NHẤT TẠI ĐÂY ---
     * Phương thức này lấy ID người dùng (UUID) một cách an toàn từ đối tượng UserDetailsImpl.
     */
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            // Ném ra một ngoại lệ rõ ràng nếu người dùng chưa được xác thực đúng cách
            throw new IllegalStateException("User is not authenticated or principal is of unexpected type.");
        }
        // Lấy principal, ép kiểu về UserDetailsImpl và lấy ID
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }

    /**
     * Lấy danh sách giao dịch đã được phân trang và lọc.
     * Controller chỉ nhận tham số và chuyển cho Service, không xử lý logic phức tạp.
     */
    @GetMapping
    public ResponseEntity<Page<Transaction>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date,desc") String[] sort,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo) {

        UUID userId = getCurrentUserId();
        // Giao toàn bộ việc lọc và phân trang cho tầng Service
        Page<Transaction> transactions = transactionService.getFilteredTransactions(
                userId, type, category, search, dateFrom, dateTo, page, size, sort);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Tạo một giao dịch mới.
     * Controller chỉ cần truyền dữ liệu và userId cho Service.
     */
    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction transaction) {
        UUID userId = getCurrentUserId();
        // Service sẽ chịu trách nhiệm liên kết giao dịch với đúng người dùng
        Transaction createdTransaction = transactionService.createTransaction(transaction, userId);
        return new ResponseEntity<>(createdTransaction, HttpStatus.CREATED);
    }

    /**
     * Cập nhật một giao dịch đã có.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable UUID id, @RequestBody Transaction transactionDetails) {
        UUID userId = getCurrentUserId();
        try {
            Transaction updatedTransaction = transactionService.updateTransaction(id, transactionDetails, userId);
            return ResponseEntity.ok(updatedTransaction);
        } catch (SecurityException e) {
            // Bắt ngoại lệ cụ thể hơn nếu người dùng không có quyền
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Xóa một giao dịch.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable UUID id) {
        UUID userId = getCurrentUserId();
        try {
            transactionService.deleteTransaction(id, userId);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}