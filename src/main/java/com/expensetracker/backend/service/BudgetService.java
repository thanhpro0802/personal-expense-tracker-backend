package com.expensetracker.backend.service;

import com.expensetracker.backend.dto.BudgetDTO;
import com.expensetracker.backend.model.Budget;
import com.expensetracker.backend.model.User;
import com.expensetracker.backend.repository.BudgetRepository;
import com.expensetracker.backend.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * Lấy tất cả ngân sách của người dùng cho một tháng,
     * đồng thời tính toán số tiền đã chi tiêu cho mỗi ngân sách đó.
     */
    public List<BudgetDTO> getBudgetsForMonth(UUID userId, int month, int year) {
        // 1. Lấy tất cả các ngân sách đã đặt
        List<Budget> budgets = budgetRepository.findByUser_IdAndMonthAndYear(userId, month, year);

        // 2. Lấy tổng chi tiêu thực tế theo từng danh mục
        List<Map<String, Object>> spentData = transactionRepository
                .findExpenseSumByCategoryAndMonthYear(userId, month, year);

        // 3. Chuyển đổi dữ liệu chi tiêu sang Map để tra cứu nhanh
        Map<String, BigDecimal> spentMap = spentData.stream()
                .collect(Collectors.toMap(
                        map -> (String) map.get("category"),
                        map -> (BigDecimal) map.get("spent")
                ));

        // 4. Tạo DTO kết quả
        return budgets.stream().map(budget -> {
            BigDecimal spentAmount = spentMap.getOrDefault(budget.getCategory(), BigDecimal.ZERO);
            BigDecimal remainingAmount = budget.getAmount().subtract(spentAmount);

            return new BudgetDTO(
                    budget.getId(),
                    budget.getCategory(),
                    budget.getAmount(),
                    spentAmount,
                    remainingAmount,
                    budget.getMonth(),
                    budget.getYear()
            );
        }).collect(Collectors.toList());
    }

    /**
     * Tạo hoặc cập nhật một ngân sách.
     * Nếu ngân sách cho danh mục/tháng/năm đó đã tồn tại, nó sẽ cập nhật số tiền.
     * Nếu chưa, nó sẽ tạo mới.
     */
    public Budget createOrUpdateBudget(Budget budgetRequest, UUID userId) {
        // Kiểm tra xem ngân sách đã tồn tại chưa
        Budget budget = budgetRepository.findByUser_IdAndCategoryAndMonthAndYear(
                userId,
                budgetRequest.getCategory(),
                budgetRequest.getMonth(),
                budgetRequest.getYear()
        ).orElse(new Budget()); // Nếu chưa, tạo mới

        // Liên kết với người dùng
        User userRef = new User();
        userRef.setId(userId);
        budget.setUser(userRef);

        // Cập nhật thông tin
        budget.setCategory(budgetRequest.getCategory());
        budget.setAmount(budgetRequest.getAmount());
        budget.setMonth(budgetRequest.getMonth());
        budget.setYear(budgetRequest.getYear());

        return budgetRepository.save(budget);
    }

    /**
     * Xóa một ngân sách.
     */
    public void deleteBudget(UUID budgetId, UUID userId) {
        Budget budget = budgetRepository.findByIdAndUser_Id(budgetId, userId)
                .orElseThrow(() -> new SecurityException("Budget not found or access denied"));

        budgetRepository.delete(budget);
    }
}