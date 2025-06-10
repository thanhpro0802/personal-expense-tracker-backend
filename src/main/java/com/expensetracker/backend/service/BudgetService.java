package com.expensetracker.backend.service;

import com.expensetracker.backend.model.Budget;
import com.expensetracker.backend.model.Category;
import com.expensetracker.backend.model.User;
import com.expensetracker.backend.repository.BudgetRepository;
import com.expensetracker.backend.repository.CategoryRepository;
import com.expensetracker.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public BudgetService(BudgetRepository budgetRepository, UserRepository userRepository, CategoryRepository categoryRepository) {
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<Budget> getAllBudgets() {
        return budgetRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Budget> getBudgetById(UUID id) {
        return budgetRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Budget> getUserBudgets(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return budgetRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public List<Budget> getUserBudgetsInPeriod(UUID userId, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return budgetRepository.findByUserAndStartDateLessThanEqualAndEndDateGreaterThanEqual(user, endDate, startDate);
    }


    @Transactional
    public Budget createBudget(Budget budget) {
        // Đảm bảo User tồn tại
        User user = userRepository.findById(budget.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + budget.getUser().getId()));
        budget.setUser(user);

        // Đảm bảo Category tồn tại và thuộc về user hoặc là default category
        Category category = categoryRepository.findById(budget.getCategory().getId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + budget.getCategory().getId()));

        if (category.getUser() != null && !category.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Category does not belong to this user or is not a default category.");
        }
        budget.setCategory(category);

        // Kiểm tra xem đã có ngân sách nào cho danh mục này trong khoảng thời gian này chưa
        List<Budget> existingBudgets = budgetRepository.findByUserAndCategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                user, category, budget.getEndDate(), budget.getStartDate()
        );
        if (!existingBudgets.isEmpty()) {
            throw new RuntimeException("A budget already exists for this category in the specified period.");
        }

        return budgetRepository.save(budget);
    }

    @Transactional
    public Budget updateBudget(UUID id, Budget budgetDetails, UUID userId) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found with id: " + id));

        // Đảm bảo người dùng sở hữu ngân sách này
        if (!budget.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access Denied: You do not have permission to update this budget.");
        }

        // Cập nhật các trường
        budget.setAmount(budgetDetails.getAmount());
        budget.setStartDate(budgetDetails.getStartDate());
        budget.setEndDate(budgetDetails.getEndDate());

        // Cập nhật category (cần kiểm tra tương tự như khi tạo)
        if (!budget.getCategory().getId().equals(budgetDetails.getCategory().getId())) {
            Category newCategory = categoryRepository.findById(budgetDetails.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("New Category not found with id: " + budgetDetails.getCategory().getId()));
            if (newCategory.getUser() != null && !newCategory.getUser().getId().equals(userId)) {
                throw new RuntimeException("New category does not belong to this user or is not a default category.");
            }
            budget.setCategory(newCategory);
        }

        // Kiểm tra trùng lặp sau khi cập nhật (ngoại trừ chính bản thân budget đang cập nhật)
        List<Budget> existingBudgets = budgetRepository.findByUserAndCategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                budget.getUser(), budget.getCategory(), budget.getEndDate(), budget.getStartDate()
        );
        boolean conflict = existingBudgets.stream()
                .anyMatch(b -> !b.getId().equals(budget.getId())); // Kiểm tra nếu có budget khác ngoài budget hiện tại
        if (conflict) {
            throw new RuntimeException("Another budget already exists for this category in the specified period.");
        }


        return budgetRepository.save(budget);
    }

    @Transactional
    public void deleteBudget(UUID id, UUID userId) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found with id: " + id));

        // Đảm bảo người dùng sở hữu ngân sách này
        if (!budget.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access Denied: You do not have permission to delete this budget.");
        }

        budgetRepository.delete(budget);
    }
}