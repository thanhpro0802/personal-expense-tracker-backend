package com.expensetracker.backend.service;

import com.expensetracker.backend.dto.DashboardStats;
import com.expensetracker.backend.dto.DashboardStats.CategoryExpense;
import com.expensetracker.backend.model.Transaction;
import com.expensetracker.backend.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;
import java.util.UUID;

@Service
public class DashboardService {

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * Lấy tất cả các chỉ số thống kê cho dashboard một cách hiệu quả.
     * Mọi tính toán tổng hợp đều được thực hiện ở tầng database.
     * @param userId ID của người dùng
     * @return Đối tượng DashboardStats chứa tất cả thông tin.
     */
    public DashboardStats getDashboardStats(UUID userId) {
        // 1. Lấy tổng thu nhập
        BigDecimal totalIncome = transactionRepository.sumAmountByTypeAndUserId(userId, Transaction.TransactionType.income);

        // 2. Lấy tổng chi tiêu
        BigDecimal totalExpenses = transactionRepository.sumAmountByTypeAndUserId(userId, Transaction.TransactionType.expense);

        // 3. Tính toán số dư hiện tại
        BigDecimal currentBalance = totalIncome.subtract(totalExpenses);

        // 4. Lấy chi tiêu theo danh mục
        List<Map<String, Object>> expenseByCategoryData = transactionRepository.findExpenseByCategory(userId);
        List<DashboardStats.CategoryExpense> expenseByCategory = expenseByCategoryData.stream()
                .map(item -> new DashboardStats.CategoryExpense(
                        (String) item.get("category"),
                        (BigDecimal) item.get("amount")
                ))
                .collect(Collectors.toList());

        // 5. Lấy dữ liệu thu/chi theo tháng
        List<Map<String, Object>> monthlySummaryData = transactionRepository.findMonthlySummary(userId);
        List<DashboardStats.MonthlyData> monthlyData = monthlySummaryData.stream()
                .map(item -> new DashboardStats.MonthlyData(
                        (String) item.get("month"),
                        (BigDecimal) item.get("income"),
                        (BigDecimal) item.get("expenses")
                ))
                .collect(Collectors.toList());

        // 6. Tạo và trả về đối tượng DTO với constructor chính xác
        return new DashboardStats(
                totalIncome,
                totalExpenses,
                currentBalance,
                monthlyData,          // <-- Đã sửa: truyền monthlyData
                expenseByCategory
        );
    }

    /**
     * Lấy các giao dịch gần đây nhất cho người dùng.
     * @param userId ID của người dùng
     * @param limit Số lượng giao dịch cần lấy
     * @return Một danh sách (List) các giao dịch
     */
    public List<Transaction> getRecentTransactions(UUID userId, int limit) {
        // Tạo yêu cầu phân trang: trang đầu tiên (0), số lượng 'limit', sắp xếp theo ngày giảm dần
        Pageable pageable = PageRequest.of(0, limit, Sort.by("date").descending());

        // Gọi repository, nó sẽ trả về một đối tượng Page<Transaction>
        Page<Transaction> transactionPage = transactionRepository.findByUserId(userId, pageable);

        // SỬA LỖI Ở ĐÂY: Lấy danh sách nội dung từ đối tượng Page
        return transactionPage.getContent();
    }
}