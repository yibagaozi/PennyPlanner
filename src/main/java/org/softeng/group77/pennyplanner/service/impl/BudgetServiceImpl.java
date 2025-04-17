package org.softeng.group77.pennyplanner.service.impl;

import org.softeng.group77.pennyplanner.model.Budget;
import org.softeng.group77.pennyplanner.service.BudgetService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class BudgetServiceImpl implements BudgetService {

    private final Map<LocalDate, Budget> budgetMap = new HashMap<>(); // 使用 Map 存储预算

    @Override
    public void saveBudget(double amount, LocalDate date) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }

        // 确保日期不为 null 且不是未来日期
        if (date == null || date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Invalid date");
        }

        Budget budget = new Budget(amount, date);
        budgetMap.put(date, budget);

        System.out.println("Saved budget: " + budget.getAmount() + " on " + budget.getDate());
    }


    @Override
    public Budget getCurrentBudget() {
        LocalDate currentDate = LocalDate.now(); // 获取当前日期
        Budget currentBudget = budgetMap.get(currentDate); // 获取当前日期的预算，若无返回 null

        // 打印获取的预算信息
        if (currentBudget != null) {
            System.out.println("Fetched current budget: " + currentBudget.getAmount() + " on " + currentBudget.getDate());
        } else {
            System.out.println("No budget found for current date.");
        }

        return currentBudget; // 返回当前日期的预算
    }

    @Override
    public Budget getBudgetByDate(LocalDate date) {
        // 检查日期是否为 null
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }

        Budget budget = budgetMap.get(date); // 获取指定日期的预算

        // 打印获取的预算信息
        if (budget != null) {
            System.out.println("Fetched budget for " + date + ": " + budget.getAmount());
        } else {
            System.out.println("No budget found for date " + date);
        }

        return budget; // 返回指定日期的预算
    }
}
