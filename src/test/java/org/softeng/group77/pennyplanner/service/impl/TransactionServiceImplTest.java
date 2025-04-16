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

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    static class TestTransactionRepository extends JsonTransactionRepositoryImpl {
        public TestTransactionRepository(String filePath) {
            super(filePath);
        }

        public TestTransactionRepository(String filePath, TypeReference<List<Transaction>> typeReference) {
            super(filePath);
        }
    }


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

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(jsonFilePath);
        Files.deleteIfExists(tempDir);
    }

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

        TransactionDetail result = transactionService.updateTransaction(updateDetail);

        assertNotNull(result);
        assertEquals(updateDetail.getAmount(), result.getAmount());
        assertEquals(updateDetail.getDescription(), result.getDescription());
        assertEquals(updateDetail.getCategory(), result.getCategory());

        List<Transaction> savedTransactions = ((JsonTransactionRepositoryImpl)transactionRepository).loadAll();
        assertEquals(1, savedTransactions.size());
        assertEquals("Updated Transaction", savedTransactions.get(0).getDescription());
        assertEquals(new BigDecimal("200.00"), savedTransactions.get(0).getAmount());
    }

    @Test
    void getTransaction_Success() throws Exception {

        Transaction transaction = createAndSaveTestTransaction();

        TransactionDetail result = transactionService.getTransaction(transaction.getId());

        assertNotNull(result);
        assertEquals(transaction.getId(), result.getId());
        assertEquals(transaction.getDescription(), result.getDescription());
        assertEquals(transaction.getAmount(), result.getAmount());
    }

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

}
