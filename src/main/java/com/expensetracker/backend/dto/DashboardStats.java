package com.expensetracker.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardStats {
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal currentBalance;
    private List<MonthlyData> monthlyData;

    // Constructor, getters, setters

    public DashboardStats(BigDecimal totalIncome, BigDecimal totalExpenses, BigDecimal currentBalance, List<MonthlyData> monthlyData) {
        this.totalIncome = totalIncome;
        this.totalExpenses = totalExpenses;
        this.currentBalance = currentBalance;
        this.monthlyData = monthlyData;
    }

    public DashboardStats() {}

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


    // Getters & Setters
}
