package org.softeng.group77.pennyplanner.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.softeng.group77.pennyplanner.dto.UserInfo;
import org.softeng.group77.pennyplanner.exception.BudgetNotFoundException;
import org.softeng.group77.pennyplanner.exception.BudgetProcessingException;
import org.softeng.group77.pennyplanner.model.Budget;
import org.softeng.group77.pennyplanner.model.Transaction;
import org.softeng.group77.pennyplanner.model.User;
import org.softeng.group77.pennyplanner.repository.impl.BudgetRepositoryImpl;
import org.softeng.group77.pennyplanner.repository.impl.JsonTransactionRepositoryImpl;
import org.softeng.group77.pennyplanner.service.AuthService;
import org.softeng.group77.pennyplanner.service.BudgetService;
import org.softeng.group77.pennyplanner.util.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class BudgetServiceImplTest {

    private String TEST_USER_ID = "user123";
    private static final String OTHER_USER_ID = "user456";
    private static final String PASSWORD = "password";

    private BudgetService budgetService;
    private TestBudgetRepository budgetRepository;
    private LocalDate testDate;

    @Mock
    private AuthService authService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private Path tempDir;
    private Path jsonFilePath;
    private UserInfo testUser;

    static class TestBudgetRepository extends BudgetRepositoryImpl {
        public TestBudgetRepository(String filePath) {
            super(filePath);
        }

        public TestBudgetRepository(String filePath, TypeReference<List<Transaction>> typeReference) {
            super(filePath);
        }
    }

    @BeforeEach
    void setUp() throws IOException {

        tempDir = Files.createTempDirectory("budget-test");
        jsonFilePath = tempDir.resolve("budget.json");
        Files.createFile(jsonFilePath);
        Files.writeString(jsonFilePath, "[]");

        User user = new User(TEST_USER_ID, passwordEncoder.encode(PASSWORD), "password", "test@example.com", "13333333333");
        testUser = UserMapper.toUserInfo(user);
        lenient().when(authService.getCurrentUser()).thenReturn(testUser);
        testDate = LocalDate.now(); // 获取当前日期

        budgetRepository = new TestBudgetRepository(jsonFilePath.toString());

        budgetService = new BudgetServiceImpl(budgetRepository, authService); // 初始化 BudgetServiceImpl

    }

    @Test
    void testSaveAndGetBudget() throws Exception {
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
    void testSaveBudgetWhenDateIsSame() throws Exception {
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
        assertThrows(BudgetProcessingException.class, () -> {
            budgetService.saveBudget(-5000, testDate);
        }, "Saving negative budget amount should throw IllegalArgumentException");
    }

    @Test
    void testSaveBudgetWithInvalidDate() {
        // 保存一个无效日期的预算，应该抛出 IllegalArgumentException
        LocalDate invalidDate = LocalDate.of(9999, 12, 31); // 使用一个极限日期
        assertThrows(BudgetProcessingException.class, () -> {
            budgetService.saveBudget(5000, invalidDate);
        }, "Saving budget with invalid date should throw IllegalArgumentException");
    }


    @Test
    void testGetBudgetByDate() throws Exception {
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

        assertThrows(BudgetProcessingException.class, () -> {
            budgetService.getBudgetByDate(testDate);
        }, "Budget cannot be null");
    }

    @Test
    void testGetCurrentBudgetWhenNoBudgetSaved() {

        assertThrows(BudgetNotFoundException.class, () -> {
            budgetService.getCurrentBudget();
        }, "No current budget");
    }

    @Test
    void testSaveBudgetWithNullDate() {
        assertThrows(BudgetProcessingException.class, () -> {
            budgetService.saveBudget(5000, null); // 传递 null 会抛出异常
        }, "Date cannot be null");
    }

    @Test
    void testSaveBudgetWithFutureDate() throws Exception {
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
    void testSaveBudgetWithZeroAmount() throws Exception {
        // 保存金额为 0 的预算
        budgetService.saveBudget(0, testDate);

        // 获取当前预算
        Budget currentBudget = budgetService.getCurrentBudget();

        // 验证金额为 0
        assertNotNull(currentBudget, "Saved budget should not be null");
        assertEquals(0, currentBudget.getAmount(), "Budget amount should be 0");
    }

    @Test
    void testGetCurrentBudgetForSameMonth() throws Exception {
        LocalDate nextMonth = LocalDate.now().plusMonths(1);

        // 创建下个月的预算，确保当前月没有预算
        budgetService.saveBudget(1000, nextMonth.withDayOfMonth(5));
        budgetService.saveBudget(1500, nextMonth.withDayOfMonth(10));
        budgetService.saveBudget(2000, nextMonth.withDayOfMonth(15));

        assertThrows(BudgetNotFoundException.class, () -> {
            budgetService.getCurrentBudget();
        }, "No current budget for the same month");

    }

    @Test
    void testSaveBudgetForPastDate() {
        // 尝试保存一个过去的日期，应该抛出 IllegalArgumentException
        assertThrows(BudgetProcessingException.class, () -> {
            budgetService.saveBudget(2000, LocalDate.of(2025, 4, 18)); // 设置过去的日期
        }, "Should not allow saving budget for past dates");
    }
}
