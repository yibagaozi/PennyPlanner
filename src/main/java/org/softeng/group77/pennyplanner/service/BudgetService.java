package org.softeng.group77.pennyplanner.service;

import org.softeng.group77.pennyplanner.model.Budget;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;

public interface BudgetService {
    // 保存预算
    Budget saveBudget(double amount, LocalDate date) throws Exception;

    // 获取当前日期的预算
    Budget getCurrentBudget() throws Exception;

    // 获取指定日期的预算
    Budget getBudgetByDate(LocalDate date) throws Exception;

    Budget getBudgetByYearMonth(YearMonth yearMonth) throws Exception;
}
