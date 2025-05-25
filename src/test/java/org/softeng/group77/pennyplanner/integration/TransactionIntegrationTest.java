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

/**
 * Integration tests for the budget and transaction management in the PennyPlanner application.
 * These tests verify the end-to-end functionality of the budgeting and transaction
 * recording systems.
 *
 * @author JIANG Mengnan
 * @version 2.0.0
 * @since 2.0.0
 */
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

    /**
     * Sets up the test environment before each test method.
     * Ensures a test user is authenticated for all tests.
     *
     * @throws Exception if authentication fails
     */
    @BeforeEach
    public void setUp() throws Exception {
        TestContext.ensureAuthenticatedUser(authService);
    }

    /**
     * Tests the budget management functionality.
     *
     * @throws Exception if any budget operation fails
     */
    @Test
    public void testBudgetManagement() throws Exception {
        System.out.println("======== Start test: Budget ========");

        LocalDate today = LocalDate.now();
        double monthlyBudget = 3000.0;
        Budget budget = budgetService.saveBudget(monthlyBudget, today);

        assertNotNull(budget, "Budget should not be null");
        assertEquals(monthlyBudget, budget.getAmount(), 0.001, "Budget amount does not match");
        assertEquals(today, budget.getDate(), "Budget date does not match");

        System.out.println("Budget create success: " + budget.getAmount());

        Budget currentBudget = budgetService.getCurrentBudget();

        assertNotNull(currentBudget, "Current budget should not be null");
        assertEquals(monthlyBudget, currentBudget.getAmount(), 0.001, "Current budget amount does not match");

        System.out.println("Current budget: " + currentBudget.getAmount());

        YearMonth nextMonth = YearMonth.now().plusMonths(1);
        LocalDate nextMonthDate = nextMonth.atDay(1);
        double nextMonthBudget = 3500.0;
        Budget nextBudget = budgetService.saveBudget(nextMonthBudget, nextMonthDate);

        assertNotNull(nextBudget, "Next month budget should not be null");
        assertEquals(nextMonthBudget, nextBudget.getAmount(), 0.001, "Next month budget amount does not match");

        System.out.println("Budget create success for next month: " + nextBudget.getAmount());

        Budget retrievedBudget = budgetService.getBudgetByYearMonth(nextMonth);

        assertNotNull(retrievedBudget, "Retrieved budget by year-month should not be null");
        assertEquals(nextMonthBudget, retrievedBudget.getAmount(), 0.001, "Retrieved budget amount does not match");

        System.out.println("Get budget success: " + retrievedBudget.getAmount());
        System.out.println("======== Budget Test Success ========");
    }

    /**
     * Tests the transaction recording and management functionality.
     *
     * @throws Exception if any transaction operation fails
     */
    @Test
    public void testTransactionRecording() throws Exception {
        System.out.println("======== Start test: Transaction ========");

        String userId = TestContext.getUserId();

        Transaction income = new Transaction();
        income.setDescription("Income");
        income.setCategory("Salary");
        income.setAmount(new BigDecimal("5000.00"));
        income.setMethod("Bank Transfer");
        income.setTransactionDateTime(LocalDateTime.now().minusDays(5));
        income.setUserId(userId);

        TransactionDetail savedIncome = transactionService.createTransaction(transactionMapper.toTransactionDetail(income));

        assertNotNull(savedIncome, "Saved income record should not be null");
        assertNotNull(savedIncome.getId(), "Income record ID should not be null");
        assertEquals(0, new BigDecimal("5000.00").compareTo(savedIncome.getAmount()), "Income amount does not match");

        System.out.println("Income: " + savedIncome.getDescription() + " - " + savedIncome.getAmount());

        BigDecimal[] expenses = {
                new BigDecimal("-220.00"),
                new BigDecimal("-150.00"),
                new BigDecimal("-80.00"),
                new BigDecimal("-100.00"),
                new BigDecimal("-100.00"),
                new BigDecimal("-100.00")
        };
        String[] descriptions = {
                "Shopping", "Dinner", "Ticket",
                "Book", "Taxi", "Rent"
        };
        String[] categories = {
                "Shopping", "Food", "Entertainment",
                "Education", "Transportation", "Living Bill"
        };
        int[] daysAgo = {3, 2, 1, 4, 6, 0};

        for (int i = 0; i < expenses.length; i++) {
            TransactionDetail tx = new TransactionDetail();
            tx.setDescription(descriptions[i]);
            tx.setCategory(categories[i]);
            tx.setAmount(expenses[i]);
            tx.setMethod("Cash");
            tx.setTransactionDateTime(LocalDateTime.now().minusDays(daysAgo[i]));
            tx.setUserId(userId);
            transactionService.createTransaction(tx);
        }

        List<TransactionDetail> allTransactions = transactionService.getUserTransactions();

        System.out.println("Transaction " + allTransactions.size() + " in total");

        List<TransactionDetail> foodTransactions = transactionService.filterTransactionByCategory("Food");

        System.out.println("Filter transaction by category, " + foodTransactions.size() + " by category Food");

        LocalDate startDate = LocalDateTime.now().minusDays(4).toLocalDate();
        LocalDate endDate = LocalDateTime.now().toLocalDate();
        List<TransactionDetail> recentTransactions = transactionService.filterTransactionByDate(startDate, endDate);

        System.out.println("Filter transaction by date, " + recentTransactions.size() + " latest transactions");

        TransactionDetail txToUpdate = foodTransactions.get(0);
        txToUpdate.setAmount(new BigDecimal("-220.00"));
        txToUpdate.setDescription("Shopping (Updated)");

        TransactionDetail updatedTx = transactionService.updateTransaction(txToUpdate);

        assertNotNull(updatedTx, "Updated transaction should not be null");
        assertEquals(0, new BigDecimal("-220.00").compareTo(updatedTx.getAmount()), "Updated amount does not match");
        assertEquals("Shopping (Updated)", updatedTx.getDescription(), "Updated description does not match");

        System.out.println("Transaction updated: " + updatedTx.getDescription() + " - " + updatedTx.getAmount());

        Map<String, Double> summary = transactionService.getDefaultSummary(LocalDateTime.now());

        assertNotNull(summary, "Financial summary should not be null");
        assertEquals(5000.0, summary.get("income"), 0.001, "Total income does not match");
        assertEquals(750.0, summary.get("expense"), 0.001, "Total expense does not match");
        assertEquals(4250.0, summary.get("totalBalance"), 0.001, "Net balance does not match");

        System.out.println("Get budget success:");
        System.out.println("  Income: " + summary.get("income"));
        System.out.println("  Expense: " + summary.get("expense"));
        System.out.println("  Balance: " + summary.get("totalBalance"));

        System.out.println("======== Transaction Test Success ========");
    }
}
