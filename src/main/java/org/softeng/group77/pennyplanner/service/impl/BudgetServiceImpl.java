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

        // 不允许保存过去日期的预算
        if (date == null || date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot set budget for past dates");
        }

        // 限制最大日期为2030-12-31
        LocalDate maxValidDate = LocalDate.of(2030, 12, 31);
        if (date.isAfter(maxValidDate)) {
            throw new IllegalArgumentException("Invalid date: exceeds maximum valid date");
        }

        Budget budget = new Budget(amount, date);
        budgetMap.put(date, budget); // 每次修改时，都会覆盖同一日期的预算

        System.out.println("Saved budget: " + budget.getAmount() + " for " + budget.getDate());
    }

    @Override
    public Budget getBudgetByDate(LocalDate date) {
        // 检查日期是否为 null
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }

        return budgetMap.get(date); // 返回指定日期的预算
    }

    @Override
    public Budget getCurrentBudget() {
        LocalDate currentDate = LocalDate.now();
        System.out.println("Current Date: " + currentDate);  // 打印当前日期

        Budget latestBudget = null;

        // 遍历所有预算，筛选当前月份的预算
        for (Map.Entry<LocalDate, Budget> entry : budgetMap.entrySet()) {
            System.out.println("Checking budget for date: " + entry.getKey()); // 打印每个预算的日期

            // 如果预算日期属于当前月份
            if (entry.getKey().getMonth() == currentDate.getMonth()) {
                if (latestBudget == null || entry.getValue().getDate().isAfter(latestBudget.getDate())) {
                    latestBudget = entry.getValue(); // 保留最新的预算
                }
            }
        }

        // 如果没有找到最新的预算
        if (latestBudget == null) {
            System.out.println("No budget found for the current month (" + currentDate.getMonth() + ")");
        }

        return latestBudget; // 返回当前月份最新的预算，如果没有则返回 null
    }

}
