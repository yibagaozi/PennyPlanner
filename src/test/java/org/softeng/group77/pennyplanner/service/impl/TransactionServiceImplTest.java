package org.softeng.group77.pennyplanner.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.softeng.group77.pennyplanner.dto.TransactionDetail;
import org.softeng.group77.pennyplanner.dto.UserInfo;
import org.softeng.group77.pennyplanner.exception.TransactionProcessingException;
import org.softeng.group77.pennyplanner.model.Transaction;
import org.softeng.group77.pennyplanner.model.User;
import org.softeng.group77.pennyplanner.repository.impl.JsonTransactionRepositoryImpl;
import org.softeng.group77.pennyplanner.service.AuthService;
import org.softeng.group77.pennyplanner.service.TransactionService;
import org.softeng.group77.pennyplanner.util.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the TransactionServiceImpl class.
 * These tests verify the transaction management functionality provided by the
 * TransactionServiceImpl, including creating, updating, deleting, retrieving,
 * and filtering transactions. The tests use a temporary JSON file for transaction
 * storage and mock the authentication service to simulate an authenticated user.
 *
 * @author CHAI Yihang
 * @version 2.0.0
 * @since 1.1.0
 */
@ExtendWith(MockitoExtension.class)
public class TransactionServiceImplTest {

    private String TEST_USER_ID = "user123";
    private static final String OTHER_USER_ID = "user456";
    private static final String PASSWORD = "password";

    private TransactionService transactionService;
    private TestTransactionRepository transactionRepository;

    @Mock
    private AuthService authService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private Path tempDir;
    private Path jsonFilePath;
    private UserInfo testUser;

    /**
     * Test implementation of JsonTransactionRepository that works with a temporary file.
     */
    static class TestTransactionRepository extends JsonTransactionRepositoryImpl {
        public TestTransactionRepository(String filePath) {
            super(filePath);
        }

        public TestTransactionRepository(String filePath, TypeReference<List<Transaction>> typeReference) {
            super(filePath);
        }
    }

    /**
     * Sets up the test environment before each test method.
     *
     * @throws IOException if file operations fail
     */
    @BeforeEach
    void setUp() throws IOException {

        tempDir = Files.createTempDirectory("transaction-test");
        jsonFilePath = tempDir.resolve("transactions.json");
        Files.createFile(jsonFilePath);
        Files.writeString(jsonFilePath, "[]");

        User user = new User(TEST_USER_ID, passwordEncoder.encode(PASSWORD), "password", "test@example.com", "13333333333");
        testUser = UserMapper.toUserInfo(user);
        lenient().when(authService.getCurrentUser()).thenReturn(testUser);

        transactionRepository = new TestTransactionRepository(jsonFilePath.toString());

        transactionService = new TransactionServiceImpl(transactionRepository, authService);
    }

    /**
     * Cleans up the test environment after each test method.
     * Deletes the temporary files created during setup.
     *
     * @throws IOException if file deletion fails
     */
    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(jsonFilePath);
        Files.deleteIfExists(tempDir);
    }

    /**
     * Tests successful transaction creation.
     *
     * @throws Exception if transaction creation fails
     */
    @Test
    void createTransaction_Success() throws Exception {

        TransactionDetail detail = createTestTransactionDetail(null);

        TransactionDetail result = transactionService.createTransaction(detail);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(detail.getAmount(), result.getAmount());
        assertEquals(detail.getDescription(), result.getDescription());
        assertEquals(TEST_USER_ID, result.getUserId());

        String jsonContent = Files.readString(jsonFilePath);
        assertTrue(jsonContent.contains(detail.getDescription()));
        assertTrue(jsonContent.contains(TEST_USER_ID));

        List<Transaction> savedTransactions = ((JsonTransactionRepositoryImpl)transactionRepository).loadAll();
        assertEquals(1, savedTransactions.size());
        assertEquals(detail.getDescription(), savedTransactions.get(0).getDescription());
    }

    /**
     * Tests transaction creation validation failure.
     */
    @Test
    void createTransaction_ValidationFailure() {

        TransactionDetail detail = new TransactionDetail();
        detail.setDescription("Test Transaction");
        detail.setCategory("Food");
        detail.setTransactionDateTime(LocalDateTime.now());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            transactionService.createTransaction(detail);
        });

        assertTrue(exception.getMessage().contains("Amount is required"));
    }

    /**
     * Tests transaction creation authentication failure.
     */
    @Test
    void createTransaction_AuthenticationFailure() {
        when(authService.getCurrentUser()).thenReturn(null);
        TransactionDetail detail = createTestTransactionDetail(null);

        Exception exception = assertThrows(TransactionProcessingException.class, () -> {
            transactionService.createTransaction(detail);
        });

        assertTrue(exception.getMessage().contains("User must be logged in")
                   || exception.getMessage().contains("authentication")
                   || exception.getMessage().contains("Authorization")
                   || exception.getMessage().contains("unexpected"));

    }

    /**
     * Tests successful transaction update.
     *
     * @throws Exception if transaction operations fail
     */
    @Test
    void updateTransaction_Success() throws Exception {

        Transaction transaction = createAndSaveTestTransaction();

        TransactionDetail updateDetail = new TransactionDetail();
        updateDetail.setId(transaction.getId());
        updateDetail.setUserId(TEST_USER_ID);
        updateDetail.setAmount(new BigDecimal("200.00"));
        updateDetail.setDescription("Updated Transaction");
        updateDetail.setCategory("Shopping");
        updateDetail.setTransactionDateTime(LocalDateTime.now());
        updateDetail.setMethod("Cash");

        TransactionDetail result = transactionService.updateTransaction(updateDetail);

        assertNotNull(result);
        assertEquals(updateDetail.getAmount(), result.getAmount());
        assertEquals(updateDetail.getDescription(), result.getDescription());
        assertEquals(updateDetail.getCategory(), result.getCategory());
        assertEquals(updateDetail.getMethod(), result.getMethod());

        List<Transaction> savedTransactions = ((JsonTransactionRepositoryImpl)transactionRepository).loadAll();
        assertEquals(1, savedTransactions.size());
        assertEquals("Updated Transaction", savedTransactions.get(0).getDescription());
        assertEquals(new BigDecimal("200.00"), savedTransactions.get(0).getAmount());
    }

    /**
     * Tests successful transaction retrieval.
     *
     * @throws Exception if transaction operations fail
     */
    @Test
    void getTransaction_Success() throws Exception {

        Transaction transaction = createAndSaveTestTransaction();

        TransactionDetail result = transactionService.getTransaction(transaction.getId());

        assertNotNull(result);
        assertEquals(transaction.getId(), result.getId());
        assertEquals(transaction.getDescription(), result.getDescription());
        assertEquals(transaction.getAmount(), result.getAmount());
    }

    /**
     * Tests successful transaction deletion.
     *
     * @throws Exception if transaction operations fail
     */
    @Test
    void deleteTransaction_Success() throws Exception {

        Transaction transaction = createAndSaveTestTransaction();

        List<Transaction> beforeDelete = ((JsonTransactionRepositoryImpl)transactionRepository).loadAll();
        assertEquals(1, beforeDelete.size());

        boolean result = transactionService.deleteTransaction(transaction.getId());

        assertTrue(result);

        List<Transaction> afterDelete = ((JsonTransactionRepositoryImpl)transactionRepository).loadAll();
        assertEquals(0, afterDelete.size());
    }

    /**
     * Tests retrieval of transactions for the current user.
     *
     * @throws Exception if transaction operations fail
     */
    @Test
    void getUserTransactions_Success() throws Exception {

        createAndSaveTestTransaction();
        createAndSaveTestTransaction();

        Transaction otherUserTx = new Transaction(OTHER_USER_ID);
        otherUserTx.setAmount(new BigDecimal("50.00"));
        otherUserTx.setDescription("Other User Transaction");
        otherUserTx.setCategory("Other");
        otherUserTx.setTransactionDateTime(LocalDateTime.now());
        transactionRepository.save(otherUserTx);

        List<TransactionDetail> results = transactionService.getUserTransactions();

        assertNotNull(results);
        assertEquals(2, results.size());

        for (TransactionDetail detail : results) {
            assertEquals(TEST_USER_ID, detail.getUserId());
        }
    }

    /**
     * Tests searching for transactions by keyword.
     *
     * @throws Exception if transaction operations fail
     */
    @Test
    void searchUserTransactions_Success() throws Exception {

        Transaction tx1 = createTestTransaction();
        tx1.setDescription("Grocery shopping");
        transactionRepository.save(tx1);

        Transaction tx2 = createTestTransaction();
        tx2.setDescription("Restaurant dinner");
        transactionRepository.save(tx2);

        Transaction tx3 = createTestTransaction();
        tx3.setDescription("Shopping for clothes");
        transactionRepository.save(tx3);

        List<TransactionDetail> results = transactionService.searchUserTransactions("shopping");

        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(t -> t.getDescription().equals("Grocery shopping")));
        assertTrue(results.stream().anyMatch(t -> t.getDescription().equals("Shopping for clothes")));
    }

    /**
     * Tests filtering transactions by date range.
     *
     * @throws Exception if transaction operations fail
     */
    @Test
    void filterTransactionByDate_Success() throws Exception {

        Transaction tx1 = createTestTransaction();
        tx1.setTransactionDateTime(LocalDateTime.of(2025, 4, 10, 10, 0));
        transactionRepository.save(tx1);

        Transaction tx2 = createTestTransaction();
        tx2.setTransactionDateTime(LocalDateTime.of(2025, 4, 12, 15, 30));
        transactionRepository.save(tx2);

        Transaction tx3 = createTestTransaction();
        tx3.setTransactionDateTime(LocalDateTime.of(2025, 4, 15, 9, 45));
        transactionRepository.save(tx3);

        LocalDate startDate = LocalDate.of(2025, 4, 11);
        LocalDate endDate = LocalDate.of(2025, 4, 14);
        List<TransactionDetail> results = transactionService.filterTransactionByDate(startDate, endDate);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(LocalDateTime.of(2025, 4, 12, 15, 30), results.get(0).getTransactionDateTime());
    }

    /**
     * Tests filtering transactions by category.
     *
     * @throws Exception if transaction operations fail
     */
    @Test
    void filterTransactionByCategory_Success() throws Exception {

        Transaction tx1 = createTestTransaction();
        tx1.setCategory("Food");
        transactionRepository.save(tx1);

        Transaction tx2 = createTestTransaction();
        tx2.setCategory("Shopping");
        transactionRepository.save(tx2);

        Transaction tx3 = createTestTransaction();
        tx3.setCategory("Food");
        transactionRepository.save(tx3);

        List<TransactionDetail> results = transactionService.filterTransactionByCategory("Food");

        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(t -> t.getCategory().equals("Food")));
    }

    private TransactionDetail createTestTransactionDetail(String id) {
        TransactionDetail detail = new TransactionDetail();
        detail.setId(id);
        detail.setUserId(TEST_USER_ID);
        detail.setAmount(new BigDecimal("100.00"));
        detail.setDescription("Test Transaction");
        detail.setCategory("Food");
        detail.setTransactionDateTime(LocalDateTime.now());
        detail.setMethod("Credit Card");
        return detail;
    }

    /**
     * Creates a test transaction detail object with specified properties.
     *
     * @param id the transaction ID (null for new transactions)
     * @return a TransactionDetail object with test values
     */
    private Transaction createTestTransaction() {
        Transaction transaction = new Transaction(TEST_USER_ID);
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setDescription("Test Transaction");
        transaction.setCategory("Food");
        transaction.setTransactionDateTime(LocalDateTime.now());
        transaction.setMethod("Credit Card");
        return transaction;
    }

    /**
     * Creates a test transaction object with default properties.
     *
     * @return a Transaction object with test values
     */
    private Transaction createAndSaveTestTransaction() throws IOException {
        Transaction transaction = createTestTransaction();
        return transactionRepository.save(transaction);
    }

    /**
     * Gets a financial summary for the current month up to a specified end time.
     *
     * @param endTime the end time for the summary period (optional)
     * @return a map containing income, expense, and balance totals
     * @throws IOException if data access fails
     */
    public Map<String, Double> getDefaultSummary(@RequestParam(required = false) LocalDateTime endTime) throws IOException {
        String userId = authService.getCurrentUser().getId();
    
        // 获取当前月份的第一天
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.withDayOfMonth(1);
    
        // 如果用户未提供结束时间，则默认为当前时间
        LocalDateTime effectiveEndTime = endTime != null ? endTime : LocalDateTime.now();
    
        // 查询从本月1号到指定时间点的数据
        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateTimeBetween(
                userId,
                firstDayOfMonth.atStartOfDay(),
                effectiveEndTime
        );
    
        return calculateSummary(transactions);
    }//这个方法是如果用户没有输入开始结束日期就返回该月1号到当前时间的那三个数值

    /**
     * Gets a financial summary for a specified date range.
     *
     * @param startDate the start date for the summary period
     * @param endDate the end date for the summary period
     * @return a map containing income, expense, and balance totals
     * @throws IOException if data access fails
     * @throws IllegalArgumentException if start date is after end date
     */
    public Map<String, Double> getSummaryByDateRange(LocalDate startDate, LocalDate endDate) throws IOException {
        // 1. 验证日期范围
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
    
        String userId = authService.getCurrentUser().getId();
    
        // 2. 获取时间范围内的交易
        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateTimeBetween(
                userId,
                startDate != null ? startDate.atStartOfDay() : null,
                endDate != null ? endDate.atTime(23, 59, 59) : null
        );
    
        // 3. 计算汇总数据
        return calculateSummary(transactions);
    }//这个方法是输入了特定时间返回三个数据

    /**
     * Calculates financial summary data from a list of transactions.
     *
     * @param transactions the list of transactions to summarize
     * @return a map containing income, expense, and balance totals
     */
    private Map<String, Double> calculateSummary(List<Transaction> transactions) {
        // 使用 BigDecimal 进行精确计算，最后转换为 Double
        BigDecimal income = transactions.stream()
                .filter(t -> t.getAmount() != null && t.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    
        BigDecimal expense = transactions.stream()
                .filter(t -> t.getAmount() != null && t.getAmount().compareTo(BigDecimal.ZERO) < 0)
                .map(t -> t.getAmount().abs()) // 支出取绝对值
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    
        BigDecimal balance = income.subtract(expense);
    
        return Map.of(
            "totalBalance", balance.doubleValue(),
            "income", income.doubleValue(),
            "expense", expense.doubleValue()
        );
    }

}
