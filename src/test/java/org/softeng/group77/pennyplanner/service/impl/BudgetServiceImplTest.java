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
        assertThrows(IllegalArgumentException.class, () -> {
            budgetService.saveBudget(5000, futureDate); // 传递未来日期会抛出异常
        }, "Invalid date");
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
}
