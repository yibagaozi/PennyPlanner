package org.softeng.group77.pennyplanner.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.softeng.group77.pennyplanner.model.Budget;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class BudgetServiceImplTest {

    private BudgetServiceImpl budgetService;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        budgetService = new BudgetServiceImpl(); // 初始化 BudgetServiceImpl
        testDate = LocalDate.now(); // 获取当前日期
    }

    @Test
    void testSaveAndGetBudget() {
        // 保存预算
        budgetService.saveBudget(5000, testDate);

        // 获取当前预算
        Budget currentBudget = budgetService.getCurrentBudget();

        // 验证预算不为 null
        assertNotNull(currentBudget, "Saved budget should not be null");
        // 验证金额和日期
        assertEquals(5000, currentBudget.getAmount(), "Budget amount should be 5000");
        assertEquals(testDate, currentBudget.getDate(), "Budget date should be the current date");
    }

    @Test
    void testSaveBudgetWhenDateIsSame() {
        // 保存第一个预算
        budgetService.saveBudget(5000, testDate);
        // 再次保存同一天的预算（覆盖）
        budgetService.saveBudget(10000, testDate);

        // 获取当前预算
        Budget currentBudget = budgetService.getCurrentBudget();

        // 验证保存的预算金额是第二次保存的金额
        assertNotNull(currentBudget, "Saved budget should not be null");
        assertEquals(10000, currentBudget.getAmount(), "Budget amount should be 10000 after overwriting");
    }

    @Test
    void testSaveBudgetWithNegativeAmount() {
        // 保存负金额预算，应该抛出 IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            budgetService.saveBudget(-5000, testDate);
        }, "Saving negative budget amount should throw IllegalArgumentException");
    }

    @Test
    void testSaveBudgetWithInvalidDate() {
        // 保存一个无效日期的预算，应该抛出 IllegalArgumentException
        LocalDate invalidDate = LocalDate.of(9999, 12, 31); // 使用一个极限日期
        assertThrows(IllegalArgumentException.class, () -> {
            budgetService.saveBudget(5000, invalidDate);
        }, "Saving budget with invalid date should throw IllegalArgumentException");
    }


    @Test
    void testGetBudgetByDate() {
        // 保存一个预算
        budgetService.saveBudget(5000, testDate);

        // 获取指定日期的预算
        Budget retrievedBudget = budgetService.getBudgetByDate(testDate);

        // 验证获取的预算与保存的预算一致
        assertNotNull(retrievedBudget, "Budget should not be null");
        assertEquals(5000, retrievedBudget.getAmount(), "Budget amount should be 5000");
        assertEquals(testDate, retrievedBudget.getDate(), "Budget date should match the requested date");
    }

    @Test
    void testGetBudgetByDateWhenNoBudgetSaved() {
        // 没有保存任何预算
        Budget retrievedBudget = budgetService.getBudgetByDate(testDate);

        // 验证返回 null，因为没有保存任何预算
        assertNull(retrievedBudget, "Budget should be null when no budget is saved for the date");
    }

    @Test
    void testGetCurrentBudgetWhenNoBudgetSaved() {
        // 没有保存任何预算
        Budget currentBudget = budgetService.getCurrentBudget();

        // 验证返回 null，因为没有保存任何预算
        assertNull(currentBudget, "Current budget should be null when no budget is saved");
    }

    @Test
    void testSaveBudgetWithNullDate() {
        assertThrows(IllegalArgumentException.class, () -> {
            budgetService.saveBudget(5000, null); // 传递 null 会抛出异常
        }, "Date cannot be null");
    }

    @Test
    void testSaveBudgetWithFutureDate() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        budgetService.saveBudget(5000, futureDate); // 现在允许设置未来日期

        // 获取当前预算
        Budget currentBudget = budgetService.getCurrentBudget();

        // 验证未来日期的预算是否被正确保存
        assertNotNull(currentBudget, "Saved future budget should not be null");
        assertEquals(5000, currentBudget.getAmount(), "Budget amount should be 5000 for future date");
        assertEquals(futureDate, currentBudget.getDate(), "Budget date should match the future date");
    }

    @Test
    void testSaveBudgetWithZeroAmount() {
        // 保存金额为 0 的预算
        budgetService.saveBudget(0, testDate);

        // 获取当前预算
        Budget currentBudget = budgetService.getCurrentBudget();

        // 验证金额为 0
        assertNotNull(currentBudget, "Saved budget should not be null");
        assertEquals(0, currentBudget.getAmount(), "Budget amount should be 0");
    }

    @Test
    void testGetCurrentBudgetForSameMonth() {
        // 保存多个预算，确保有不同日期的预算
        budgetService.saveBudget(1000, LocalDate.of(2025, 5, 5));  // 5月5号的预算
        budgetService.saveBudget(1500, LocalDate.of(2025, 5, 10)); // 5月10号的预算
        budgetService.saveBudget(2000, LocalDate.of(2025, 5, 15)); // 5月15号的预算

        // 假设今天是 2025年4月19日，当前月没有预算
        // 获取当月最新的预算（在此例中应打印出 "No budget found for the current month (APRIL)"）
        Budget currentBudget = budgetService.getCurrentBudget();

        // 验证打印出提示信息，表示当前月没有预算
        // 输出信息应该是： "No budget found for the current month (APRIL)"
        assertNull(currentBudget, "No budget should be returned for the current month (April)");
    }


    @Test
    void testSaveBudgetForPastDate() {
        // 尝试保存一个过去的日期，应该抛出 IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            budgetService.saveBudget(2000, LocalDate.of(2025, 4, 18)); // 设置过去的日期
        }, "Should not allow saving budget for past dates");
    }
}
