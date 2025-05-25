package org.softeng.group77.pennyplanner.repository;

import org.softeng.group77.pennyplanner.model.Budget;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository interface for Budget entity operations.
 * Provides methods to create, read, update and delete budget records.
 *
 * @author JIANG Mengnan
 * @version 2.0.0
 * @since 1.2.0
 */
public interface BudgetRepository {

    /**
     * Saves a budget to the repository
     *
     * @param budget the budget to save
     * @return the saved budget with any generated fields
     * @throws IOException if a data access error occurs
     */
    Budget save(Budget budget) throws IOException;

    /**
     * Deletes a budget for the specified date
     *
     * @param date the date of the budget to delete
     * @return true if deletion was successful, false otherwise
     * @throws IOException if a data access error occurs
     */
    boolean deleteByDate(LocalDate date) throws IOException;

    /**
     * Finds a budget for the specified date
     *
     * @param date the date to search for
     * @return an Optional containing the budget if found
     * @throws IOException if a data access error occurs
     */
    Optional<Budget> findByDate(LocalDate date) throws IOException;

    /**
     * Retrieves all budgets from the repository
     *
     * @return a map of budgets with date as the key
     * @throws IOException if a data access error occurs
     */
    Map<LocalDate, Budget> findAll() throws IOException;

    /**
     * Updates an existing budget for the specified date
     *
     * @param date the date of the budget to update
     * @param updatedBudget the new budget data
     * @return an Optional containing the updated budget if successful
     * @throws IOException if a data access error occurs
     */
    Optional<Budget> update(LocalDate date, Budget updatedBudget) throws IOException;

    /**
     * Checks if a budget exists for the specified date
     *
     * @param date the date to check
     * @return true if a budget exists, false otherwise
     * @throws IOException if a data access error occurs
     */
    boolean exists(LocalDate date) throws IOException;

    /**
     * Counts the total number of budgets in the repository
     *
     * @return the count of budgets
     * @throws IOException if a data access error occurs
     */
    long count() throws IOException;

    /**
     * Removes all budgets from the repository
     *
     * @throws IOException if a data access error occurs
     */
    void clear() throws IOException;
}
