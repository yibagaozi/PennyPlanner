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

/**
 * Integration tests for the AI-powered transaction analysis features.
 * The tests use real AI service calls rather than mocks to ensure end-to-end
 * functionality. Each test prepares sample transaction data and validates
 * that the AI services return appropriate, non-empty responses.
 *
 * @author JIANG Mengnan
 * @version 2.0.0
 * @since 2.0.0
 */
@SpringBootTest
public class TransactionAnalysisIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionAnalysisService transactionAnalysisService;

    private final int TIMEOUT_SECONDS = 180;

    /**
     * Sets up the test environment before each test method.
     * Creates a test user if none exists and ensures sufficient transaction data is available.
     *
     * @throws Exception if setup operations fail
     */
    @BeforeEach
    public void setUp() throws Exception {
        TestContext.ensureAuthenticatedUser(authService);
        ensureTransactionData();
    }

    /**
     * Ensures that sufficient transaction data exists for testing.
     * creates sample transactions including income and various expense categories.
     *
     * @throws Exception if transaction creation fails
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

    /**
     * Helper method to create a transaction record for testing.
     *
     * @param userId the user ID to associate with the transaction
     * @param description the transaction description
     * @param category the transaction category
     * @param amount the transaction amount (positive for income, negative for expense)
     * @param dateTime the transaction date and time
     * @throws Exception if transaction creation fails
     */
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
     * Tests the transaction classification functionality.
     *
     * @throws ExecutionException if the classification operation fails
     * @throws InterruptedException if the operation is interrupted
     * @throws TimeoutException if the operation times out
     */
    @Test
    public void testTransactionClassification() throws ExecutionException, InterruptedException, TimeoutException {
        System.out.println("======== Start test: Transaction Classification ========");

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
            assertNotNull(category, "Classification result should not be null");
            assertFalse(category.isEmpty(), "Classification result should not be empty");
            System.out.println("Description \"" + description + "\" categorized: " + category);

            // 稍作延迟避免API速率限制
            Thread.sleep(1000);
        }

        System.out.println("======== Classification Test Finished ========");
    }

    /**
     * Tests the spending analysis report generation functionality.
     *
     * @throws ExecutionException if the report generation operation fails
     * @throws InterruptedException if the operation is interrupted
     * @throws TimeoutException if the operation times out
     */
    @Test
    public void testSpendingAnalysisReport() throws ExecutionException, InterruptedException, TimeoutException {
        System.out.println("======== Start test: Report Generate ========");

        // 设置日期范围（过去一个月）
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(1);

        System.out.println("Generate report range from " + startDate + " to " + endDate);

        // 调用实际报告生成服务
        CompletableFuture<String> reportFuture = transactionAnalysisService.generateSpendingAnalysisReport(
                startDate, endDate);

        // 获取报告内容
        String report = reportFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        // 验证报告内容
        assertNotNull(report, "Generated report should not be null");
        assertFalse(report.isEmpty(), "Generated report should not be empty");

        // 输出报告摘要
        System.out.println("Report generate finished:");
        String[] reportLines = report.split("\n");
        for (int i = 0; i < Math.min(20, reportLines.length); i++) {
            System.out.println(reportLines[i]);
        }
        if (reportLines.length > 20) {
            System.out.println("... [Too long] ...");
        }

        System.out.println("======== Report Generate Test Finished ========");
    }

}
