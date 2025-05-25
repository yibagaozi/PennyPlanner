package org.softeng.group77.pennyplanner.service;

import org.softeng.group77.pennyplanner.dto.TransactionDetail;
import org.softeng.group77.pennyplanner.model.Transaction;
import org.springframework.stereotype.Component;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Provides transaction management operations.
 * Handles creating, retrieving, updating, and analyzing financial transactions.
 *
 * @author MA Ruize
 * @author XI Yu
 * @version 2.0.0
 * @since 1.0.0
 */
@Component
public interface TransactionService {

    /**
     * Creates a new transaction record
     *
     * @param transactionDetail the transaction data to save
     * @return the created transaction with generated ID
     */
    TransactionDetail createTransaction(TransactionDetail transactionDetail);

    /**
     * Updates an existing transaction
     *
     * @param transactionDetail the transaction data with updates
     * @return the updated transaction
     */
    TransactionDetail updateTransaction(TransactionDetail transactionDetail);

    /**
     * Retrieves a specific transaction by ID
     *
     * @param transactionId the ID of the transaction to retrieve
     * @return the requested transaction
     */
    TransactionDetail getTransaction(String transactionId);

    /**
     * Deletes a transaction
     *
     * @param transactionId the ID of the transaction to delete
     * @return true if deletion was successful
     */
    boolean deleteTransaction(String transactionId);

    /**
     * Gets all transactions for the current user
     *
     * @return list of the user's transactions
     */
    List<TransactionDetail> getUserTransactions();

    /**
     * Searches transactions using a keyword
     *
     * @param keyword the search term
     * @return list of matching transactions
     */
    List<TransactionDetail> searchUserTransactions(String keyword);

    /**
     * Filters transactions by date range
     *
     * @param startDate the beginning of the date range
     * @param endDate the end of the date range
     * @return list of transactions within the date range
     */
    List<TransactionDetail> filterTransactionByDate(LocalDate startDate, LocalDate endDate);

    /**
     * Retrieves transactions by date range for analysis purposes
     *
     * @param startDate the beginning of the date range
     * @param endDate the end of the date range
     * @return list of transaction model objects for analysis
     */
    List<Transaction> filterTransactionByDateForAnalysis(LocalDate startDate, LocalDate endDate);

    /**
     * Filters transactions by category
     *
     * @param category the category to filter by
     * @return list of transactions in the specified category
     */
    List<TransactionDetail> filterTransactionByCategory(String category);

    /**
     * Gets a financial summary up to the specified time
     *
     * @param endTime the end time for the summary period
     * @return map of summary metrics and their values
     * @throws IOException if data cannot be accessed
     */
    Map<String, Double> getDefaultSummary( LocalDateTime endTime) throws IOException;

    /**
     * Gets a financial summary for a specific date range
     *
     * @param startDate the beginning of the date range
     * @param endDate the end of the date range
     * @return map of summary metrics and their values
     * @throws IOException if data cannot be accessed
     */
    Map<String, Double> getSummaryByDateRange(LocalDate startDate, LocalDate endDate) throws IOException;

    /**
     * Filters transactions by payment method
     *
     * @param method the payment method to filter by
     * @return list of transactions using the specified payment method
     */
    List<TransactionDetail> filterTransactionByMethod(String method);

}
    
