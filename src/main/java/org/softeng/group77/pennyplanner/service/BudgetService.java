package org.softeng.group77.pennyplanner.service;

import org.softeng.group77.pennyplanner.model.Budget;
import java.time.LocalDate;

public interface BudgetService {
    // 保存预算
    void saveBudget(double amount, LocalDate date);

    // 获取当前日期的预算
    Budget getCurrentBudget();

    // 获取指定日期的预算
    Budget getBudgetByDate(LocalDate date);
}

