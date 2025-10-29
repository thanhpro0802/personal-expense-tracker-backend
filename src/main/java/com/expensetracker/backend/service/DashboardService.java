package com.expensetracker.backend.service;

import com.expensetracker.backend.dto.DashboardStats;
import com.expensetracker.backend.dto.DashboardStats.CategoryExpense;
import com.expensetracker.backend.model.Transaction;
import com.expensetracker.backend.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WalletService walletService;

    public DashboardStats getDashboardStats(UUID walletId, UUID userId, int month, int year) {
        // Kiểm tra quyền truy cập
        if (!walletService.isUserMemberOfWallet(walletId, userId)) {
            throw new SecurityException("User is not a member of this wallet");
        }

        // --- TÍNH TOÁN DỮ LIỆU CHO THÁNG HIỆN TẠI ---

        // 1. Lấy tổng thu nhập CHO THÁNG ĐÃ CHỌN
        BigDecimal totalIncomeForMonth = transactionRepository.sumAmountByTypeAndMonthAndYear(walletId, Transaction.TransactionType.income, month, year);

        // 2. Lấy tổng chi tiêu CHO THÁNG ĐÃ CHỌN
        BigDecimal totalExpensesForMonth = transactionRepository.sumAmountByTypeAndMonthAndYear(walletId, Transaction.TransactionType.expense, month, year);

        // 3. Lấy chi tiêu theo danh mục cho tháng
        List<Map<String, Object>> expenseByCategoryData = transactionRepository.findExpenseByCategoryAndMonthAndYear(walletId, month, year);
        List<CategoryExpense> expenseByCategory = expenseByCategoryData.stream()
                .map(item -> new CategoryExpense(
                        (String) item.get("category"),
                        (BigDecimal) item.get("amount")
                ))
                .collect(Collectors.toList());

        // 4. Lấy các giao dịch gần đây cho tháng
        List<Transaction> recentTransactions = transactionRepository.findRecentTransactionsByMonthAndYear(walletId, month, year, PageRequest.of(0, 5));

        // --- TÍNH TOÁN SỐ DƯ TỔNG CỘNG (CUMULATIVE BALANCE) ---
        // --- SỬA LỖI LOGIC QUAN TRỌNG NHẤT Ở ĐÂY ---

        // 5a. Lấy tổng thu nhập TỪ TRƯỚC ĐẾN NAY (sử dụng phương thức cũ không có bộ lọc tháng/năm)
        BigDecimal totalIncomeAllTime = transactionRepository.sumAmountByTypeAndWalletId(walletId, Transaction.TransactionType.income);

        // 5b. Lấy tổng chi tiêu TỪ TRƯỚC ĐẾN NAY
        BigDecimal totalExpensesAllTime = transactionRepository.sumAmountByTypeAndWalletId(walletId, Transaction.TransactionType.expense);

        // 5c. Tính toán số dư TỔNG CỘNG
        BigDecimal cumulativeBalance = totalIncomeAllTime.subtract(totalExpensesAllTime);


        // Dữ liệu lịch sử không cần thiết cho màn hình này
        List<DashboardStats.MonthlyData> monthlyData = List.of();

        // 6. Tạo và trả về DTO với dữ liệu ĐÚNG theo yêu cầu
        return new DashboardStats(
                totalIncomeForMonth,      // <-- Dữ liệu của tháng
                totalExpensesForMonth,    // <-- Dữ liệu của tháng
                cumulativeBalance,        // <-- Dữ liệu TỔNG CỘNG
                monthlyData,
                expenseByCategory,
                recentTransactions
        );
    }
}