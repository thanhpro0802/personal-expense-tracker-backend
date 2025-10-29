package com.expensetracker.backend.service;

import com.expensetracker.backend.dto.BudgetDTO;
import com.expensetracker.backend.model.Budget;
import com.expensetracker.backend.model.Wallet;
import com.expensetracker.backend.repository.BudgetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private WalletService walletService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Lấy tất cả ngân sách của ví cho một tháng.
     * Số tiền đã chi tiêu được lấy trực tiếp từ đối tượng Budget.
     */
    public List<BudgetDTO> getBudgetsForMonth(UUID walletId, UUID userId, int month, int year) {
        // Kiểm tra quyền truy cập
        if (!walletService.isUserMemberOfWallet(walletId, userId)) {
            throw new SecurityException("User is not a member of this wallet");
        }

        // 1. Lấy tất cả các ngân sách đã đặt
        List<Budget> budgets = budgetRepository.findByWallet_IdAndMonthAndYear(walletId, month, year);

        // 2. Chuyển đổi sang DTO
        return budgets.stream().map(budget -> {
            BigDecimal remainingAmount = budget.getAmount().subtract(budget.getSpentAmount());

            return new BudgetDTO(
                    budget.getId(),
                    budget.getCategory(),
                    budget.getAmount(),
                    budget.getSpentAmount(), // Lấy trực tiếp từ budget
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
    public Budget createOrUpdateBudget(Budget budgetRequest, UUID walletId, UUID userId) {
        // Kiểm tra quyền truy cập
        if (!walletService.isUserMemberOfWallet(walletId, userId)) {
            throw new SecurityException("User is not a member of this wallet");
        }

        // Tìm budget đã có, nếu không thì tạo mới và spentAmount sẽ mặc định là 0
        Budget budget = budgetRepository.findByWallet_IdAndCategoryAndMonthAndYear(
                walletId,
                budgetRequest.getCategory(),
                budgetRequest.getMonth(),
                budgetRequest.getYear()
        ).orElse(new Budget());

        // Liên kết với ví
        Wallet walletRef = new Wallet();
        walletRef.setId(walletId);
        budget.setWallet(walletRef);

        // Cập nhật thông tin
        budget.setCategory(budgetRequest.getCategory());
        budget.setAmount(budgetRequest.getAmount());
        budget.setMonth(budgetRequest.getMonth());
        budget.setYear(budgetRequest.getYear());

        // Nếu là budget mới, spentAmount sẽ là 0 theo mặc định trong Entity
        if (budget.getId() == null) {
            budget.setSpentAmount(BigDecimal.ZERO);
        }

        Budget savedBudget = budgetRepository.save(budget);

        // Gửi thông báo WebSocket
        messagingTemplate.convertAndSend("/topic/wallet/" + walletId, 
                Map.of("message", "DATA_UPDATED", "type", "BUDGET_UPDATED"));

        return savedBudget;
    }

    /**
     * Xóa một ngân sách.
     */
    public void deleteBudget(UUID budgetId, UUID walletId, UUID userId) {
        // Kiểm tra quyền truy cập
        if (!walletService.isUserMemberOfWallet(walletId, userId)) {
            throw new SecurityException("User is not a member of this wallet");
        }

        Budget budget = budgetRepository.findByIdAndWallet_Id(budgetId, walletId)
                .orElseThrow(() -> new SecurityException("Budget not found or access denied"));

        budgetRepository.delete(budget);

        // Gửi thông báo WebSocket
        messagingTemplate.convertAndSend("/topic/wallet/" + walletId, 
                Map.of("message", "DATA_UPDATED", "type", "BUDGET_DELETED"));
    }
}