package org.softeng.group77.pennyplanner.integration;

import org.softeng.group77.pennyplanner.dto.TransactionDetail;
import org.softeng.group77.pennyplanner.service.AuthService;
import org.softeng.group77.pennyplanner.service.TransactionAnalysisService;
import org.softeng.group77.pennyplanner.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TransactionAnalysisIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionAnalysisService transactionAnalysisService;

    private final int TIMEOUT_SECONDS = 180;

    @BeforeEach
    public void setUp() throws Exception {
        TestContext.ensureAuthenticatedUser(authService);
        ensureTransactionData();
    }

    /**
     * 确保有足够的交易数据用于测试
     */
    private void ensureTransactionData() throws Exception {
        List<TransactionDetail> transactions = transactionService.getUserTransactions();
        if (transactions.size() < 5) {
            // 创建一些测试交易数据
            String userId = TestContext.getUserId();

            // 收入
            createTransaction(userId, "Monthly Salary", "Income", 5000.00, LocalDateTime.now().minusDays(20));

            // 支出
            createTransaction(userId, "Grocery Shopping at Supermarket", "Food", -200.00, LocalDateTime.now().minusDays(15));
            createTransaction(userId, "Dinner at Italian Restaurant", "Food", -120.00, LocalDateTime.now().minusDays(10));
            createTransaction(userId, "Monthly Rent Payment", "Living Bill", -1200.00, LocalDateTime.now().minusDays(5));
            createTransaction(userId, "Taxi to Airport", "Transportation", -80.00, LocalDateTime.now().minusDays(3));
            createTransaction(userId, "Movie Tickets and Popcorn", "Entertainment", -50.00, LocalDateTime.now().minusDays(1));
        }
    }

    private void createTransaction(String userId, String description, String category, double amount,
                                   LocalDateTime dateTime) throws Exception {
        TransactionDetail txDetail = new TransactionDetail();
        txDetail.setDescription(description);
        txDetail.setCategory(category);
        txDetail.setAmount(new BigDecimal(String.valueOf(amount)));
        txDetail.setMethod("Credit Card");
        txDetail.setTransactionDateTime(dateTime);
        txDetail.setUserId(userId);

        transactionService.createTransaction(txDetail);
    }

    /**
     * 测试交易分类功能
     * 直接调用真实AI服务进行分类
     */
    @Test
    public void testTransactionClassification() throws ExecutionException, InterruptedException, TimeoutException {
        System.out.println("======== 开始测试: 交易分类 (实际AI调用) ========");

        // 准备测试的交易描述
        String[] descriptions = {
                "Restaurant dinner with friends",
                "Taxi ride to airport",
                "Movie tickets and popcorn",
                "Monthly rent payment",
                "School tuition fee payment"
        };

        for (String description : descriptions) {
            // 调用实际分类服务
            CompletableFuture<String> categoryFuture = transactionAnalysisService.classifyTransaction(description);

            // 获取分类结果
            String category = categoryFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            // 验证返回的分类结果
            assertNotNull(category, "分类结果不应为空");
            assertFalse(category.isEmpty(), "分类结果不应为空字符串");
            System.out.println("交易描述 \"" + description + "\" 被分类为: " + category);

            // 稍作延迟避免API速率限制
            Thread.sleep(1000);
        }

        System.out.println("======== 交易分类测试完成 ========");
    }

    /**
     * 测试支出分析报告生成功能
     * 直接调用真实AI服务生成报告
     */
    @Test
    public void testSpendingAnalysisReport() throws ExecutionException, InterruptedException, TimeoutException {
        System.out.println("======== 开始测试: 支出分析报告 (实际AI调用) ========");

        // 设置日期范围（过去一个月）
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(1);

        System.out.println("生成日期范围 " + startDate + " 到 " + endDate + " 的支出分析报告");

        // 调用实际报告生成服务
        CompletableFuture<String> reportFuture = transactionAnalysisService.generateSpendingAnalysisReport(
                startDate, endDate);

        // 获取报告内容
        String report = reportFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        // 验证报告内容
        assertNotNull(report, "报告不应为空");
        assertFalse(report.isEmpty(), "报告不应为空字符串");

        // 输出报告摘要
        System.out.println("支出分析报告生成成功，报告摘要:");
        String[] reportLines = report.split("\n");
        for (int i = 0; i < Math.min(10, reportLines.length); i++) {
            System.out.println(reportLines[i]);
        }
        if (reportLines.length > 10) {
            System.out.println("... [报告内容较长，已省略] ...");
        }

        System.out.println("======== 支出分析报告测试完成 ========");
    }

}
