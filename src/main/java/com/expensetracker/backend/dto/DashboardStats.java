package com.expensetracker.backend.dto;

import com.expensetracker.backend.model.Transaction; // <-- 1. THÊM IMPORT

import java.math.BigDecimal;
import java.util.List;

public class DashboardStats {
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal currentBalance;
    private List<MonthlyData> monthlyData;
    private List<CategoryExpense> expenseByCategory;
    private List<Transaction> recentTransactions; // <-- 2. THÊM TRƯỜNG MỚI

    // Constructor, getters, setters

    // <-- 3. CẬP NHẬT CONSTRUCTOR ĐỂ NHẬN THAM SỐ THỨ 6 -->
    public DashboardStats(BigDecimal totalIncome, BigDecimal totalExpenses, BigDecimal currentBalance, List<MonthlyData> monthlyData, List<CategoryExpense> expenseByCategory, List<Transaction> recentTransactions) {
        this.totalIncome = totalIncome;
        this.totalExpenses = totalExpenses;
        this.currentBalance = currentBalance;
        this.monthlyData = monthlyData;
        this.expenseByCategory = expenseByCategory;
        this.recentTransactions = recentTransactions; // <-- Gán giá trị cho trường mới
    }

    public DashboardStats() {}

    // --- 4. THÊM GETTER VÀ SETTER CHO TRƯỜNG MỚI ---
    public List<Transaction> getRecentTransactions() {
        return recentTransactions;
    }

    public void setRecentTransactions(List<Transaction> recentTransactions) {
        this.recentTransactions = recentTransactions;
    }
    // ----------------------------------------------------

    public List<CategoryExpense> getExpenseByCategory() {
        return expenseByCategory;
    }

    public void setExpenseByCategory(List<CategoryExpense> expenseByCategory) {
        this.expenseByCategory = expenseByCategory;
    }

    // Getters & Setters
    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public List<MonthlyData> getMonthlyData() {
        return monthlyData;
    }

    public void setMonthlyData(List<MonthlyData> monthlyData) {
        this.monthlyData = monthlyData;
    }
    public static class CategoryExpense {
        private String category;
        private BigDecimal amount;

        public CategoryExpense(String category, BigDecimal amount) {
            this.category = category;
            this.amount = amount;
        }

        // Getters and Setters
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }

    public static class MonthlyData {
        private String month; // e.g. "2025-07"
        private BigDecimal income;
        private BigDecimal expenses;

        // Constructor, getters, setters
        public MonthlyData(String month, BigDecimal income, BigDecimal expenses) {
            this.month = month;
            this.income = income;
            this.expenses = expenses;
        }

        public MonthlyData() {}

        // Getter
        public BigDecimal getIncome() {
            return income;
        }

        // Setter
        public void setIncome(BigDecimal income) {
            this.income = income;
        }

        public BigDecimal getExpenses() {
            return expenses;
        }

        public void setExpenses(BigDecimal expenses) {
            this.expenses = expenses;
        }

        public String getMonth() {
            return month;
        }

        public void setMonth(String month) {
            this.month = month;
        }
    }
}