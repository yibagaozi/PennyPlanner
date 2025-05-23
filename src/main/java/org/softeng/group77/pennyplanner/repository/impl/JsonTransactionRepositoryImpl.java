package org.softeng.group77.pennyplanner.repository.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.softeng.group77.pennyplanner.model.Transaction;
import org.softeng.group77.pennyplanner.repository.TransactionRepository;
import org.softeng.group77.pennyplanner.repository.base.JsonDataManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
/**
 * JSON-based implementation of the TransactionRepository.
 *
 * Stores and manages Transaction objects using a JSON file as the persistence mechanism.
 */
@Repository
@Slf4j
public class JsonTransactionRepositoryImpl extends JsonDataManager<Transaction> implements TransactionRepository {

    /**
     * Constructs the JSON repository with a file path.
     *
     * @param filePath the file path for storing transaction data in JSON format,
     *                 configurable via application properties.
     */
    public JsonTransactionRepositoryImpl(@Value("${app.data.path:data}/transaction.json") String filePath) {
        super(filePath, new TypeReference<>() {});
    }
    /**
     * Saves a transaction by first removing any existing transaction with the same ID.
     *
     * @param transaction the transaction to save
     * @return the saved transaction
     * @throws IOException if an I/O error occurs during saving
     */
    @Override
    public Transaction save(Transaction transaction) throws IOException {
        delete(existing -> existing.getId().equals(transaction.getId()));
        return super.save(transaction);
    }
    /**
     * Deletes a transaction by its ID.
     *
     * @param id the ID of the transaction to delete
     * @return true if a transaction was deleted; false otherwise
     * @throws IOException if an I/O error occurs during deletion
     */

    @Override
    public boolean deleteById(String id) throws IOException {
        return delete(transaction -> transaction.getId().equals(id));
    }
    /**
     * Finds a transaction by its ID.
     *
     * @param id the ID of the transaction
     * @return an Optional containing the found transaction, or empty if not found
     * @throws IOException if an I/O error occurs during the search
     */
    @Override
    public Optional<Transaction> findById(String id) throws IOException {
        return findOne(transaction -> transaction.getId().equals(id));
    }
    /**
     * Finds all transactions for a given user ID.
     *
     * @param userId the user ID to filter transactions
     * @return a list of transactions associated with the given user ID
     * @throws IOException if an I/O error occurs during the search
     */
    @Override
    public List<Transaction> findByUserId(String userId) throws IOException {
        return findAll(transaction -> transaction.getUserId() != null && transaction.getUserId().equals(userId));
    }
    /**
     * Finds all transactions for a given user ID and matching description (case-insensitive).
     *
     * @param userId the user ID to filter transactions
     * @param description the description text to search within each transaction
     * @return a list of matching transactions
     * @throws IOException if an I/O error occurs during the search
     */
    @Override
    public List<Transaction> findByUserIdAndDescription(String userId, String description) throws IOException {
        return findAll(transaction ->
        transaction.getUserId() != null &&
        transaction.getUserId().equals(userId) &&
        transaction.getDescription() != null &&
        transaction.getDescription().toLowerCase().contains(description.toLowerCase())
        );
    }

}
