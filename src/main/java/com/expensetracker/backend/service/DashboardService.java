package com.expensetracker.backend.service;

import com.expensetracker.backend.dto.DashboardStats;
import com.expensetracker.backend.model.Transaction;
import com.expensetracker.backend.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DashboardService {

    @Autowired
    private TransactionRepository transactionRepository;

    public DashboardStats getDashboardStats(UUID userId) {
        List<Transaction> transactions = transactionRepository.findByUserId(userId);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        Map<String, DashboardStats.MonthlyData> monthlyDataMap = new TreeMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (Transaction t : transactions) {
            if (t.getType() == Transaction.TransactionType.income) {
                totalIncome = totalIncome.add(t.getAmount());
            } else if (t.getType() == Transaction.TransactionType.expense) {
                totalExpenses = totalExpenses.add(t.getAmount());
            }

            String month = t.getDate().format(formatter);
            monthlyDataMap.putIfAbsent(month, new DashboardStats.MonthlyData(month, BigDecimal.ZERO, BigDecimal.ZERO));
            DashboardStats.MonthlyData md = monthlyDataMap.get(month);

            if (t.getType() == Transaction.TransactionType.income) {
                md.setIncome(md.getIncome().add(t.getAmount()));
            } else if (t.getType() == Transaction.TransactionType.expense) {
                md.setExpenses(md.getExpenses().add(t.getAmount()));
            }
        }

        BigDecimal currentBalance = totalIncome.subtract(totalExpenses);

        return new DashboardStats(totalIncome, totalExpenses, currentBalance, new ArrayList<>(monthlyDataMap.values()));
    }

    public List<Transaction> getRecentTransactions(UUID userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit, org.springframework.data.domain.Sort.by("date").descending());
        return transactionRepository.findByUserId(userId, pageable);
    }
}