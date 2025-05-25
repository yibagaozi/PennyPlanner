package org.softeng.group77.pennyplanner.repository;

import org.softeng.group77.pennyplanner.model.Transaction;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Transaction entity operations.
 * Provides methods to persist, retrieve, and manage financial transactions.
 *
 * @author MA Ruize
 * @author XI Yu
 * @version 2.0.0
 * @since 1.0.0
 */
public interface TransactionRepository {

    /**
     * Saves a transaction to the repository
     *
     * @param transaction the transaction to save
     * @return the saved transaction with any generated fields
     * @throws IOException if a data access error occurs
     */
    Transaction save (Transaction transaction) throws IOException;

    /**
     * Deletes a transaction by its ID
     *
     * @param id the ID of the transaction to delete
     * @return true if deletion was successful, false otherwise
     * @throws IOException if a data access error occurs
     */
    boolean deleteById(String id) throws IOException;

    /**
     * Finds a transaction by its ID
     *
     * @param id the ID of the transaction to find
     * @return an Optional containing the transaction if found
     * @throws IOException if a data access error occurs
     */
    Optional<Transaction> findById(String id) throws IOException;

    /**
     * Finds all transactions for a specific user
     *
     * @param userId the ID of the user
     * @return a list of transactions belonging to the user
     * @throws IOException if a data access error occurs
     */
    List<Transaction> findByUserId(String userId) throws IOException;

    /**
     * Finds transactions for a user that match a description
     *
     * @param userId the ID of the user
     * @param description the description to search for
     * @return a list of matching transactions
     * @throws IOException if a data access error occurs
     */
    List<Transaction> findByUserIdAndDescription(String userId, String description) throws IOException;

    /**
     * Finds transactions for a user within a date range
     *
     * @param userId the ID of the user
     * @param start the start of the date range
     * @param end the end of the date range
     * @return a list of transactions within the specified period
     * @throws IOException if a data access error occurs
     */
    List<Transaction> findByUserIdAndTransactionDateTimeBetween(
        String userId,
        LocalDateTime start,
        LocalDateTime end
        ) throws IOException;

}
