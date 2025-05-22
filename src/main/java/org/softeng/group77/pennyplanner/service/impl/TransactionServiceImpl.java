package org.softeng.group77.pennyplanner.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.softeng.group77.pennyplanner.annotation.RequiresAuthentication;
import org.softeng.group77.pennyplanner.dto.TransactionDetail;
import org.softeng.group77.pennyplanner.dto.UserInfo;
import org.softeng.group77.pennyplanner.exception.TransactionNotFoundException;
import org.softeng.group77.pennyplanner.exception.TransactionProcessingException;
import org.softeng.group77.pennyplanner.repository.TransactionRepository;
import org.softeng.group77.pennyplanner.service.AuthService;
import org.softeng.group77.pennyplanner.service.TransactionService;
import org.softeng.group77.pennyplanner.model.Transaction;
import org.softeng.group77.pennyplanner.util.TransactionMapper;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final AuthService authService;

    /**
     * Constructor to initialize the TransactionServiceImpl instance through dependency injection.
     * Initializes the TransactionRepository, TransactionMapper, and AuthService dependencies.
     *
     * @param transactionRepository The TransactionRepository instance used for accessing transaction data.
     * @param authService The AuthService instance used for authentication-related operations.
     */
    public TransactionServiceImpl(TransactionRepository transactionRepository, AuthService authService) {
        this.transactionRepository = transactionRepository;
        this.transactionMapper = new TransactionMapper();
        this.authService = authService;
    }

    /**
     * Creates a new transaction record for the currently authenticated user.
     * Validates the provided transaction details, creates a new Transaction entity
     * linked to the current user, saves it to the repository, and returns the
     * saved transaction details.
     *
     * @param transactionDetail The TransactionDetail object containing the details for the new transaction.
     * @return The TransactionDetail object of the newly created and saved transaction.
     * @throws IllegalArgumentException If the transaction details are invalid.
     * @throws IOException If an I/O error occurs during the saving process (although wrapped in BudgetProcessingException in catch).
     * @throws TransactionProcessingException If an error occurs during the saving process or an unexpected error occurs.
     */
    @Override
    @RequiresAuthentication
    public TransactionDetail createTransaction(TransactionDetail transactionDetail) {

        try {
            validateTransactionDetail(transactionDetail);
            log.info("Creating transaction: {}", transactionDetail);

            String userId = authService.getCurrentUser().getId();

            Transaction transaction = new Transaction(userId);
            updateTransactionFromDetail(transaction, transactionDetail);

            Transaction savedTransaction = transactionRepository.save(transaction);
            log.info("Transaction created: {}", savedTransaction);

            return transactionMapper.toTransactionDetail(savedTransaction);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid transaction data: {}", e.getMessage());
            throw e;
        } catch (IOException e) {
            log.error("File error while saving transaction", e);
            throw new TransactionProcessingException("Failed to save transaction", e);
        } catch (Exception e) {
            log.error("Unexpected error creating transaction", e);
            throw new TransactionProcessingException("An unexpected error occurred", e);
        }
    }

    /**
     * Updates an existing transaction record for the currently authenticated user.
     * Validates the provided transaction details, finds the transaction by ID,
     * verifies that the transaction belongs to the current user, updates the entity
     * with new details, saves the changes to the repository, and returns the updated
     * transaction details.
     *
     * @param transactionDetail The TransactionDetail object containing the ID of the transaction to update and the new details.
     * @return The TransactionDetail object of the updated transaction.
     * @throws TransactionProcessingException If the transaction ID is null or empty, transaction details are invalid,
     *                                         the transaction is not found, an I/O error occurs, or an unexpected error occurs.
     * @throws AuthenticationException If the user ID in the transaction detail does not match the currently authenticated user's ID,
     *                                 or if the user is not logged in (as indicated by the implementation's exception handling).
     */
    @Override
    @RequiresAuthentication
    public TransactionDetail updateTransaction(TransactionDetail transactionDetail) {
        try {
            if (transactionDetail.getId() == null || transactionDetail.getId().isEmpty()) {
                throw new IllegalArgumentException("Transaction ID cannot be null or empty for update");
            }
            String userId = authService.getCurrentUser().getId();

            validateTransactionDetail(transactionDetail);

            Transaction existingTransaction = transactionRepository.findById(transactionDetail.getId())
                    .orElseThrow(() -> new TransactionNotFoundException("Transaction not found with ID: " + transactionDetail.getId()));

            if (!existingTransaction.getUserId().equals(transactionDetail.getUserId())||!existingTransaction.getUserId().equals(userId)) {
                throw new AuthenticationException("User ID mismatch. Cannot update transaction belonging to another user.");
            }

            updateTransactionFromDetail(existingTransaction, transactionDetail);
            existingTransaction.setUpdatedAt(LocalDateTime.now());

            Transaction updatedTransaction = transactionRepository.save(existingTransaction);

            return transactionMapper.toTransactionDetail(updatedTransaction);
        } catch (IllegalArgumentException e) {
            throw new TransactionProcessingException("Invalid transaction data: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new TransactionProcessingException("File error while updating transaction", e);
        } catch (AuthenticationException e){
            throw new TransactionProcessingException("User must be logged in to update transactions", e);
        } catch (Exception e) {
            log.error("Unexpected error updating transaction", e);
            throw new TransactionProcessingException("An unexpected error occurred", e);
        }
    }

    /**
     * Retrieves a specific transaction record by its ID for the currently authenticated user.
     * Finds the transaction by ID and verifies that it belongs to the current user
     * before returning the transaction details.
     *
     * @param transactionId The ID of the transaction to retrieve.
     * @return The TransactionDetail object of the retrieved transaction.
     * @throws TransactionNotFoundException If the transaction with the specified ID is not found.
     * @throws AuthenticationException If the user ID of the found transaction does not match the currently authenticated user's ID.
     * @throws TransactionProcessingException If an I/O error occurs during retrieval (as wrapped in the catch block) or an unexpected error occurs.
     */
    @Override
    @RequiresAuthentication
    public TransactionDetail getTransaction(String transactionId) {
        try {
            String userId = authService.getCurrentUser().getId();

            Transaction transaction = transactionRepository.findById(transactionId)
                    .orElseThrow(() -> new TransactionNotFoundException("Transaction not found with ID: " + transactionId));

            if (!transaction.getUserId().equals(userId)) {
                throw new AuthenticationException("User ID mismatch. Cannot access transaction belonging to another user.");
            }

            return transactionMapper.toTransactionDetail(transaction);

        } catch (IOException e) {
            log.error("File error while deleting transaction", e);
            throw new TransactionProcessingException("Failed to delete transaction", e);
        } catch (AuthenticationException e) {
            log.error("User must be logged in to delete transactions", e);
            throw new TransactionProcessingException("User must be logged in to delete transactions", e);
        } catch (Exception e) {
            log.error("Unexpected error deleting transaction", e);
            throw new TransactionProcessingException("An unexpected error occurred", e);
        }
    }

    /**
     * Deletes a specific transaction record by its ID for the currently authenticated user.
     * Finds the transaction by ID, verifies that it belongs to the current user, and attempts to delete it from the repository.
     *
     * @param transactionId The ID of the transaction to delete.
     * @return True if the transaction was successfully deleted.
     * @throws TransactionNotFoundException If the transaction with the specified ID is not found before or after attempting deletion.
     * @throws AuthenticationException If the user ID of the found transaction does not match the currently authenticated user's ID.
     * @throws TransactionProcessingException If an I/O error occurs during deletion or an unexpected error occurs.
     */
    @Override
    @RequiresAuthentication
    public boolean deleteTransaction(String transactionId) {
        try {
            String userId = authService.getCurrentUser().getId();

            Transaction transaction = transactionRepository.findById(transactionId)
                    .orElseThrow(() -> new TransactionNotFoundException("Transaction not found with ID: " + transactionId));

            if (!transaction.getUserId().equals(userId)) {
                throw new AuthenticationException("User ID mismatch. Cannot delete transaction belonging to another user.");
            }

            boolean deleted = transactionRepository.deleteById(transactionId);
            if (!deleted) {
                throw new TransactionNotFoundException("Transaction not found with ID: " + transactionId);
            } else {
                log.info("Transaction with ID {} deleted successfully", transactionId);
                return true;
            }
        } catch (IOException e) {
            log.error("File error while deleting transaction", e);
            throw new TransactionProcessingException("Failed to delete transaction", e);
        } catch (AuthenticationException e) {
            log.error("User must be logged in to delete transactions", e);
            throw new TransactionProcessingException("User must be logged in to delete transactions", e);
        } catch (Exception e) {
            log.error("Unexpected error deleting transaction", e);
            throw new TransactionProcessingException("An unexpected error occurred", e);
        }
    }

    /**
     * Retrieves all transaction records for the currently authenticated user.
     * Finds transactions associated with the current user's ID from the repository.
     * Returns a list of TransactionDetail objects, or an empty list if no transactions are found.
     *
     * @return A list of TransactionDetail objects belonging to the current user.
     * @throws TransactionProcessingException If an error occurs during fetching transactions (e.g., I/O error) or an unexpected error occurs.
     */
    @Override
    @RequiresAuthentication
    public List<TransactionDetail> getUserTransactions() {
        try {
            String userId = authService.getCurrentUser().getId();

            List<Transaction> transactions = transactionRepository.findByUserId(userId);

            if (transactions.isEmpty()) {
                log.info("No transactions found for user: {}", authService.getCurrentUser().getUsername());
                return List.of();
            } else {
                log.info("Found {} transactions for user: {}", transactions.size(), authService.getCurrentUser().getUsername());
                return transactionMapper.toTransactionDetailList(transactions);
            }
        } catch (IOException e) {
            log.error("File error while fetching user transactions", e);
            throw new TransactionProcessingException("Failed to fetch user transactions", e);
        } catch (Exception e) {
            log.error("Unexpected error fetching user transactions", e);
            throw new TransactionProcessingException("An unexpected error occurred", e);
        }
    }

    /**
     * Searches for transaction records belonging to the currently authenticated user that contain the specified keyword in their description.
     * Retrieves transactions for the current user and filters them based on the presence of the keyword in the transaction description.
     * Returns a list of matching TransactionDetail objects, or an empty list if no matching transactions are found.
     *
     * @param keyword The keyword to search for within transaction descriptions.
     * @return A list of matching TransactionDetail objects for the current user.
     * @throws TransactionProcessingException If an error occurs during the search process (e.g., I/O error) or an unexpected error occurs.
     */
    @Override
    @RequiresAuthentication
    public List<TransactionDetail> searchUserTransactions(String keyword) {
        try {
            String userId = authService.getCurrentUser().getId();

            List<Transaction> transactions = transactionRepository.findByUserIdAndDescription(userId, keyword);

            if (transactions.isEmpty()) {
                log.info("No transactions found for user: {} with keyword: {}", authService.getCurrentUser().getUsername(), keyword);
                return List.of();
            } else {
                log.info("Found {} transactions for user: {} with keyword: {}", transactions.size(), authService.getCurrentUser().getUsername(), keyword);
                return transactionMapper.toTransactionDetailList(transactions);
            }
        } catch (IOException e) {
            log.error("File error while searching user transactions", e);
            throw new TransactionProcessingException("Failed to search user transactions", e);
        } catch (Exception e) {
            log.error("Unexpected error searching user transactions", e);
            throw new TransactionProcessingException("An unexpected error occurred", e);
        }
    }

    /**
     * Filters transaction records for the currently authenticated user within a specified date range.
     * Retrieves all transactions for the current user and filters them based on the provided start and end dates (inclusive).
     * If either startDate or endDate is null, that boundary is not applied.
     * Returns a list of matching TransactionDetail objects, or an empty list if no transactions are found or none match the date range.
     *
     * @param startDate The start date of the date range to filter transactions (inclusive). Can be null to include transactions from the beginning.
     * @param endDate The end date of the date range to filter transactions (inclusive). Can be null to include transactions up to the end.
     * @return A list of TransactionDetail objects for the current user within the specified date range.
     * @throws TransactionProcessingException If an error occurs during the filtering process (e.g., I/O error) or an unexpected error occurs.
     */
    @Override
    @RequiresAuthentication
    public List<TransactionDetail> filterTransactionByDate(LocalDate startDate, LocalDate endDate) {
        try {
            String userId = authService.getCurrentUser().getId();

            List<Transaction> transactions = transactionRepository.findByUserId(userId);

            if (transactions.isEmpty()) {
                log.info("No transactions found for user: {} in the specified date range", authService.getCurrentUser().getUsername());
                return List.of();
            }

            List<Transaction> filteredTransactions = transactions.stream()
                    .filter(transaction -> {
                        LocalDate transactionDate = transaction.getTransactionDateTime().toLocalDate();
                        return (startDate == null || !transactionDate.isBefore(startDate)) &&
                               (endDate == null || !transactionDate.isAfter(endDate));
                    })
                    .toList();

            if (filteredTransactions.isEmpty()) {
                log.info("No transactions found for user: {} in the specified date range", authService.getCurrentUser().getUsername());
                return List.of();
            } else {
                log.info("Found {} transactions for user: {} in the specified date range", filteredTransactions.size(), authService.getCurrentUser().getUsername());
                return transactionMapper.toTransactionDetailList(filteredTransactions);
            }

        } catch (IOException e) {
            log.error("File error while filtering transactions by date", e);
            throw new TransactionProcessingException("Failed to filter transactions by date", e);
        } catch (Exception e) {
            log.error("Unexpected error filtering transactions by date", e);
            throw new TransactionProcessingException("An unexpected error occurred", e);
        }
    }

    /**
     * Filters transaction records for the currently authenticated user by a specified category.
     * Retrieves all transactions for the current user and filters them based on the provided category (case-insensitive).
     * Returns a list of matching TransactionDetail objects, or an empty list if no transactions are found or none match the category.
     *
     * @param category The category to filter transactions by.
     * @return A list of TransactionDetail objects for the current user within the specified category.
     * @throws TransactionProcessingException If an error occurs during the filtering process (e.g., I/O error) or an unexpected error occurs.
     */
    @Override
    @RequiresAuthentication
    public List<TransactionDetail> filterTransactionByCategory(String category) {
        try {
            String userId = authService.getCurrentUser().getId();

            List<Transaction> transactions = transactionRepository.findByUserId(userId);

            if (transactions.isEmpty()) {
                log.info("No transactions found for user: {} in the specified category", authService.getCurrentUser().getUsername());
                return List.of();
            }

            List<Transaction> filteredTransactions = transactions.stream()
                    .filter(transaction -> transaction.getCategory() != null && transaction.getCategory().equalsIgnoreCase(category))
                    .toList();

            if (filteredTransactions.isEmpty()) {
                log.info("No transactions found for user: {} in the specified category", authService.getCurrentUser().getUsername());
                return List.of();
            } else {
                log.info("Found {} transactions for user: {} in the specified category", filteredTransactions.size(), authService.getCurrentUser().getUsername());
                return transactionMapper.toTransactionDetailList(filteredTransactions);
            }

        } catch (IOException e) {
            log.error("File error while filtering transactions by category", e);
            throw new TransactionProcessingException("Failed to filter transactions by category", e);
        } catch (Exception e) {
            log.error("Unexpected error filtering transactions by category", e);
            throw new TransactionProcessingException("An unexpected error occurred", e);
        }
    }

    /**
     * Filters transaction records for the currently authenticated user by a specified payment method.
     * Retrieves all transactions for the current user and filters them based on the provided payment method (case-insensitive).
     * Returns a list of matching TransactionDetail objects, or an empty list if no transactions are found or none match the payment method.
     *
     * @param method The payment method to filter transactions by.
     * @return A list of TransactionDetail objects for the current user within the specified payment method.
     * @throws TransactionProcessingException If an error occurs during the filtering process (e.g., I/O error) or an unexpected error occurs.
     */
    @Override
    @RequiresAuthentication
    public List<TransactionDetail> filterTransactionByMethod(String method) {
        try {
            String userId = authService.getCurrentUser().getId();

            List<Transaction> transactions = transactionRepository.findByUserId(userId);

            if (transactions.isEmpty()) {
                log.info("No transactions found for user: {} in the specified payment method", authService.getCurrentUser().getUsername());
                return List.of();
            }

            List<Transaction> filteredTransactions = transactions.stream()
                    .filter(transaction -> transaction.getMethod() != null && transaction.getMethod().equalsIgnoreCase(method))
                    .toList();

            if (filteredTransactions.isEmpty()) {
                log.info("No transactions found for user: {} in the specified payment method", authService.getCurrentUser().getUsername());
                return List.of();
            } else {
                log.info("Found {} transactions for user: {} in the specified payment method", filteredTransactions.size(), authService.getCurrentUser().getUsername());
                return transactionMapper.toTransactionDetailList(filteredTransactions);
            }

        } catch (IOException e) {
            log.error("File error while filtering transactions by payment method", e);
            throw new TransactionProcessingException("Failed to filter transactions by payment method", e);
        } catch (Exception e) {
            log.error("Unexpected error filtering transactions by payment method", e);
            throw new TransactionProcessingException("An unexpected error occurred", e);
        }
    }

    private void validateTransactionDetail(TransactionDetail transactionDetail) {
        if (transactionDetail == null) {
            throw new IllegalArgumentException("Transaction detail cannot be null");
        }
        if (transactionDetail.getAmount() == null) {
            throw new IllegalArgumentException("Amount is required");
        }
        if (transactionDetail.getDescription() == null || transactionDetail.getDescription().isEmpty()) {
            throw new IllegalArgumentException("Description is required");
        }
        if (transactionDetail.getTransactionDateTime() == null) {
            throw new IllegalArgumentException("Transaction date and time is required");
        }
        if (transactionDetail.getCategory() == null || transactionDetail.getCategory().isEmpty()) {
            throw new IllegalArgumentException("Category is required");
        }
        if (transactionDetail.getMethod() == null || transactionDetail.getMethod().isEmpty()) {
            throw new IllegalArgumentException("Payment method is required");
        }
    }

    private void updateTransactionFromDetail(Transaction transaction, TransactionDetail transactionDetail) {
        transaction.setAmount(transactionDetail.getAmount());
        transaction.setDescription(transactionDetail.getDescription());
        transaction.setCategory(transactionDetail.getCategory());
        transaction.setTransactionDateTime(transactionDetail.getTransactionDateTime());
        transaction.setMethod(transactionDetail.getMethod());
    }

    /**
     * Calculates and returns the transaction summary (e.g., income, expense, balance)
     * for the period from the first day of the current month up to a specified end time.
     * If the end time is not provided (null), the current time is used as the end time.
     *
     * @param endTime The optional end time (inclusive) for the summary calculation. If null, the current time is used.
     * @return A Map containing String keys representing summary types (e.g., "income", "expense", "balance") and Double values representing the calculated amounts.
     * @throws IOException If an I/O error occurs during the retrieval of transactions.
     */
    public Map<String, Double> getDefaultSummary( LocalDateTime endTime) throws IOException {
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
     * Calculates and returns the transaction summary (e.g., income, expense, balance)
     * for the specified date range.
     *
     * @param startDate The start date (inclusive) of the date range. Can be null to include transactions from the beginning.
     * @param endDate The end date (inclusive, up to the end of the day) of the date range. Can be null to include transactions up to the end.
     * @return A Map containing String keys representing summary types (e.g., "income", "expense", "balance") and Double values representing the calculated amounts.
     * @throws IllegalArgumentException If the end date is before the start date.
     * @throws IOException If an I/O error occurs during the retrieval of transactions.
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
