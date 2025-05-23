package org.softeng.group77.pennyplanner.integration;

import org.softeng.group77.pennyplanner.model.Budget;
import org.softeng.group77.pennyplanner.dto.TransactionDetail;
import org.softeng.group77.pennyplanner.model.Transaction;
import org.softeng.group77.pennyplanner.service.AuthService;
import org.softeng.group77.pennyplanner.service.BudgetService;
import org.softeng.group77.pennyplanner.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.softeng.group77.pennyplanner.util.TransactionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TransactionIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionMapper transactionMapper;

    @BeforeEach
    public void setUp() throws Exception {
        TestContext.ensureAuthenticatedUser(authService);
    }

    @Test
    public void testBudgetManagement() throws Exception {
        System.out.println("======== 开始测试: 预算管理 ========");

        LocalDate today = LocalDate.now();
        double monthlyBudget = 3000.0;
        Budget budget = budgetService.saveBudget(monthlyBudget, today);

        assertNotNull(budget, "预算不应为空");
        assertEquals(monthlyBudget, budget.getAmount(), 0.001, "预算金额不匹配");
        assertEquals(today, budget.getDate(), "预算日期不匹配");

        System.out.println("本月预算创建成功: " + budget.getAmount());

        Budget currentBudget = budgetService.getCurrentBudget();

        assertNotNull(currentBudget, "当前预算不应为空");
        assertEquals(monthlyBudget, currentBudget.getAmount(), 0.001, "当前预算金额不匹配");

        System.out.println("当前预算获取成功: " + currentBudget.getAmount());

        YearMonth nextMonth = YearMonth.now().plusMonths(1);
        LocalDate nextMonthDate = nextMonth.atDay(1);
        double nextMonthBudget = 3500.0;
        Budget nextBudget = budgetService.saveBudget(nextMonthBudget, nextMonthDate);

        assertNotNull(nextBudget, "下月预算不应为空");
        assertEquals(nextMonthBudget, nextBudget.getAmount(), 0.001, "下月预算金额不匹配");

        System.out.println("下月预算创建成功: " + nextBudget.getAmount());

        Budget retrievedBudget = budgetService.getBudgetByYearMonth(nextMonth);

        assertNotNull(retrievedBudget, "按年月获取的预算不应为空");
        assertEquals(nextMonthBudget, retrievedBudget.getAmount(), 0.001, "按年月获取的预算金额不匹配");

        System.out.println("按年月获取预算成功: " + retrievedBudget.getAmount());
        System.out.println("======== 预算管理测试完成 ========");
    }

    @Test
    public void testTransactionRecording() throws Exception {
        System.out.println("======== 开始测试: 交易记录与查询 ========");

        String userId = TestContext.getUserId();

        Transaction income = new Transaction();
        income.setDescription("工资收入");
        income.setCategory("工资");
        income.setAmount(new BigDecimal("5000.00"));
        income.setMethod("银行转账");
        income.setTransactionDateTime(LocalDateTime.now().minusDays(5));
        income.setUserId(userId);

        TransactionDetail savedIncome = transactionService.createTransaction(transactionMapper.toTransactionDetail(income));

        assertNotNull(savedIncome, "保存的收入记录不应为空");
        assertNotNull(savedIncome.getId(), "收入记录ID不应为空");
        assertEquals(0, new BigDecimal("5000.00").compareTo(savedIncome.getAmount()), "收入金额不匹配");

        System.out.println("收入记录成功: " + savedIncome.getDescription() + " - " + savedIncome.getAmount());

        BigDecimal[] expenses = {
                new BigDecimal("-220.00"),
                new BigDecimal("-150.00"),
                new BigDecimal("-80.00"),
                new BigDecimal("-100.00"),
                new BigDecimal("-100.00"),
                new BigDecimal("-100.00")
        };
        String[] descriptions = {
                "超市购物", "餐厅晚餐", "电影票",
                "测试支出记录 1", "测试支出记录 2", "测试支出记录 3"
        };
        String[] categories = {
                "食品", "餐饮", "娱乐",
                "测试", "测试", "测试"
        };
        int[] daysAgo = {3, 2, 1, 4, 6, 0};

        for (int i = 0; i < expenses.length; i++) {
            TransactionDetail tx = new TransactionDetail();
            tx.setDescription(descriptions[i]);
            tx.setCategory(categories[i]);
            tx.setAmount(expenses[i]);
            tx.setMethod("现金");
            tx.setTransactionDateTime(LocalDateTime.now().minusDays(daysAgo[i]));
            tx.setUserId(userId);
            transactionService.createTransaction(tx);
        }

        List<TransactionDetail> allTransactions = transactionService.getUserTransactions();

        assertNotNull(allTransactions, "交易记录列表不应为空");
        assertEquals(7, allTransactions.size(), "应有7笔交易记录");

        System.out.println("获取所有交易记录成功，共" + allTransactions.size() + "笔");

        List<TransactionDetail> foodTransactions = transactionService.filterTransactionByCategory("食品");

        assertNotNull(foodTransactions, "按类别过滤的交易记录不应为空");
        assertEquals(1, foodTransactions.size(), "应有1笔食品类别的交易记录");

        System.out.println("按类别过滤交易成功，找到" + foodTransactions.size() + "笔食品类别交易");

        LocalDate startDate = LocalDateTime.now().minusDays(4).toLocalDate();
        LocalDate endDate = LocalDateTime.now().toLocalDate();
        List<TransactionDetail> recentTransactions = transactionService.filterTransactionByDate(startDate, endDate);

        assertNotNull(recentTransactions, "按日期过滤的交易记录不应为空");
        assertTrue(recentTransactions.size() >= 4, "应有至少4笔最近日期的交易记录");

        System.out.println("按日期过滤交易成功，找到" + recentTransactions.size() + "笔最近交易");

        TransactionDetail txToUpdate = foodTransactions.get(0);
        txToUpdate.setAmount(new BigDecimal("-220.00"));
        txToUpdate.setDescription("超市购物 (已更新)");

        TransactionDetail updatedTx = transactionService.updateTransaction(txToUpdate);

        assertNotNull(updatedTx, "更新的交易记录不应为空");
        assertEquals(0, new BigDecimal("-220.00").compareTo(updatedTx.getAmount()), "更新后的金额不匹配");
        assertEquals("超市购物 (已更新)", updatedTx.getDescription(), "更新后的描述不匹配");

        System.out.println("交易记录更新成功: " + updatedTx.getDescription() + " - " + updatedTx.getAmount());

        Map<String, Double> summary = transactionService.getDefaultSummary(LocalDateTime.now());

        assertNotNull(summary, "财务摘要不应为空");
        assertEquals(5000.0, summary.get("income"), 0.001, "收入总计不匹配");
        assertEquals(750.0, summary.get("expense"), 0.001, "支出总计不匹配");
        assertEquals(4250.0, summary.get("totalBalance"), 0.001, "净余额不匹配");

        System.out.println("获取财务摘要成功:");
        System.out.println("  收入总计: " + summary.get("income"));
        System.out.println("  支出总计: " + summary.get("expense"));
        System.out.println("  净余额: " + summary.get("totalBalance"));

        System.out.println("======== 交易记录与查询测试完成 ========");
    }
}
