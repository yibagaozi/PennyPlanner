package org.softeng.group77.pennyplanner.service.impl;

import org.softeng.group77.pennyplanner.model.Budget;
import org.softeng.group77.pennyplanner.repository.BudgetRepository;
import org.softeng.group77.pennyplanner.service.AuthService;
import org.softeng.group77.pennyplanner.service.BudgetService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import java.util.Optional;

@Service
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final AuthService authService;

    public BudgetServiceImpl(BudgetRepository budgetRepository, AuthService authService) {
        this.budgetRepository = budgetRepository;
        this.authService = authService;
    }

    @Override
    public Budget saveBudget(double amount, LocalDate date) throws IOException {
        try {
            // 验证用户登录状态
            if (authService.getCurrentUser() == null) {
                throw new IllegalStateException("No user is logged in");
            }

            // 保持原有验证逻辑
            if (amount < 0) {
                throw new IllegalArgumentException("Amount cannot be negative");
            }

            // 不允许保存过去日期的预算
            if (date == null || date.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Cannot set budget for past dates");
            }

            // 限制最大日期为2030-12-31
            LocalDate maxValidDate = LocalDate.of(2030, 12, 31);
            if (date.isAfter(maxValidDate)) {
                throw new IllegalArgumentException("Invalid date: exceeds maximum valid date");
            }

            // 创建并保存预算
            Budget budget = new Budget(amount, date);
            budgetRepository.save(budget);

            System.out.println("Saved budget: " + budget.getAmount() + " for " + budget.getDate());

            // 返回创建的预算对象
            return budget;
        } catch (Exception e) {
            System.err.println("Error saving budget: " + e.getMessage());
            throw new IOException("Error saving budget", e);
        }
    }

    @Override
    public Budget getBudgetByDate(LocalDate date) throws IOException {
        try {
            // 检查日期是否为 null
            if (date == null) {
                throw new IllegalArgumentException("Date cannot be null");
            }

            Optional<Budget> budgetOpt = budgetRepository.findByDate(date);
            if (budgetOpt.isPresent()) {
                Budget budget = budgetOpt.get();
                System.out.println("Retrieved budget: " + budget.getAmount() + " for " + budget.getDate());
                return budget;
            } else {
                throw new IllegalArgumentException("No budget found for the specified date: " + date);
            }
        } catch (IOException e) {
            System.err.println("Error retrieving budget by date: " + e.getMessage());
            throw new IOException("Error retrieving budget by date", e);
        }
    }

    @Override
    public Budget getCurrentBudget() throws IOException {
        try {
            LocalDate currentDate = LocalDate.now();
            System.out.println("Current Date: " + currentDate);  // 打印当前日期

            Budget latestBudget = null;
            Map<LocalDate, Budget> allBudgets = budgetRepository.findAll();

            for (Map.Entry<LocalDate, Budget> entry : allBudgets.entrySet()) {
                System.out.println("Checking budget for date: " + entry.getKey());

                // 如果预算日期属于当前月份和年份
                if (entry.getKey().getMonth() == currentDate.getMonth() &&
                    entry.getKey().getYear() == currentDate.getYear()) {
                    if (latestBudget == null || entry.getValue().getDate().isAfter(latestBudget.getDate())) {
                        latestBudget = entry.getValue(); // 保留最新的预算
                    }
                }
            }

            // 如果没有找到最新的预算
            if (latestBudget == null) {
                System.out.println("No budget found for the current month (" + currentDate.getMonth() + ")");
            }

            return latestBudget;
        } catch (IOException e) {
            System.err.println("Error retrieving current budget: " + e.getMessage());
            throw new IOException("Error retrieving current budget", e);
        }
    }

    @Override
    public Budget getBudgetByYearMonth(YearMonth yearMonth) throws IOException {
        try {
            if (yearMonth == null) {
                throw new IllegalArgumentException("YearMonth cannot be null");
            }

            Budget latestBudget = null;
            Map<LocalDate, Budget> allBudgets = budgetRepository.findAll();

            // 遍历所有预算，筛选指定年月的预算
            for (Map.Entry<LocalDate, Budget> entry : allBudgets.entrySet()) {
                LocalDate budgetDate = entry.getKey();
                YearMonth budgetYearMonth = YearMonth.from(budgetDate);

                if (budgetYearMonth.equals(yearMonth)) {
                    if (latestBudget == null || budgetDate.isAfter(latestBudget.getDate())) {
                        latestBudget = entry.getValue();
                    }
                }
            }

            return latestBudget;
        } catch (IOException e) {
            System.err.println("Error retrieving budget by year-month: " + e.getMessage());
            throw new IOException("Error retrieving budget by year-month", e);
        }
    }
}
