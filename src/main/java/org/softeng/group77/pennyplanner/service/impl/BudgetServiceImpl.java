package org.softeng.group77.pennyplanner.service.impl;

import org.softeng.group77.pennyplanner.model.Budget;
import org.softeng.group77.pennyplanner.service.AuthService;
import org.softeng.group77.pennyplanner.service.BudgetService;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

@Service
public class BudgetServiceImpl implements BudgetService {

    private final Map<YearMonth, Budget> budgetMap = new HashMap<>();
    private final String BUDGET_FILE = "data/budget.dat";
    private final AuthService authService;

    public BudgetServiceImpl(AuthService authService) {
        this.authService = authService;
        loadBudgetData();
    }

    @Override
    public void saveBudget(double amount, LocalDate date) {
        try {
            // 获取当前用户ID
            String userId = authService.getCurrentUser().getId();
            if (userId == null) {
                throw new IllegalStateException("No user is logged in");
            }

            // 创建预算对象
            Budget budget = new Budget(amount, date);

            // 存储当月预算
            YearMonth yearMonth = YearMonth.from(date);
            budgetMap.put(yearMonth, budget);

            // 保存到文件
            saveBudgetData();

            System.out.println("Budget saved successfully: " + amount + " for " + yearMonth);
        } catch (Exception e) {
            System.err.println("Failed to save budget: " + e.getMessage());
        }
    }

    @Override
    public Budget getCurrentBudget() {
        YearMonth currentMonth = YearMonth.now();
        return budgetMap.get(currentMonth);
    }

    @Override
    public Budget getBudgetByDate(LocalDate date) {
        YearMonth yearMonth = YearMonth.from(date);
        return budgetMap.get(yearMonth);
    }

    // 保存预算数据到文件
    private void saveBudgetData() {
        File directory = new File("data");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(BUDGET_FILE))) {
            oos.writeObject(new HashMap<>(budgetMap));
        } catch (IOException e) {
            System.err.println("Error saving budget data: " + e.getMessage());
        }
    }

    // 从文件加载预算数据
    @SuppressWarnings("unchecked")
    private void loadBudgetData() {
        File file = new File(BUDGET_FILE);
        if (!file.exists()) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(BUDGET_FILE))) {
            Map<YearMonth, Budget> loadedMap = (Map<YearMonth, Budget>) ois.readObject();
            budgetMap.clear();
            budgetMap.putAll(loadedMap);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading budget data: " + e.getMessage());
        }
    }

}