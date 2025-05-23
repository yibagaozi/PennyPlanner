package org.softeng.group77.pennyplanner.repository.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.softeng.group77.pennyplanner.model.Budget;
import org.softeng.group77.pennyplanner.repository.BudgetRepository;
import org.softeng.group77.pennyplanner.repository.base.JsonDataManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Optional;

/**
 * JSON-based implementation of the BudgetRepository.
 *
 * This class handles all budget-related data operations using a local JSON file for persistence.
 */
@Repository
@Slf4j
public class BudgetRepositoryImpl extends JsonDataManager<Budget> implements BudgetRepository {

    /**
     * Constructs a new BudgetRepository with the path to the JSON data file.
     *
     * @param filePath the file path for storing budget data in JSON format,
     *                 configurable via application properties
     */
    public BudgetRepositoryImpl(@Value("${app.data.path:data}/budget.json") String filePath) {
        super(filePath, new TypeReference<List<Budget>>() {});
    }

    /**
     * Saves a new budget. If a budget for the same date already exists, it will be replaced.
     *
     * @param budget the budget to save
     * @return the saved budget
     * @throws IOException if an I/O error occurs while saving
     */
    @Override
    public Budget save(Budget budget) throws IOException {
        delete(existing -> existing.getDate().equals(budget.getDate()));
        return super.save(budget);
    }

    /**
     * Deletes the budget for the specified date.
     *
     * @param date the date of the budget to delete
     * @return true if a budget was deleted; false otherwise
     * @throws IOException if an I/O error occurs while deleting
     */
    @Override
    public boolean deleteByDate(LocalDate date) throws IOException {
        return delete(budget -> budget.getDate().equals(date));
    }

    /**
     * Finds a budget by its date.
     *
     * @param date the date of the budget to find
     * @return an Optional containing the budget if found, or empty if not found
     * @throws IOException if an I/O error occurs while reading the data
     */
    @Override
    public Optional<Budget> findByDate(LocalDate date) throws IOException {
        return findOne(budget -> budget.getDate().equals(date));
    }

    /**
     * Retrieves all budgets stored in the JSON file.
     *
     * @return a list of all budgets
     * @throws IOException if an I/O error occurs while reading the data
     */
    @Override

    public Map<LocalDate, Budget> findAll() throws IOException {
        List<Budget> budgets = loadAll();

        // 将List<Budget>转换为Map<LocalDate, Budget>
        return budgets.stream()
            .collect(Collectors.toMap(
                Budget::getDate,  // 使用日期作为键
                budget -> budget, // 值就是预算对象本身
                (existing, replacement) -> replacement // 如果有重复，保留后者
            ));
    }

    /**
     * Updates an existing budget for a specific date.
     *
     * @param date the date of the budget to update
     * @param updatedBudget the updated budget data
     * @return an Optional containing the updated budget if successful, or empty otherwise
     * @throws IOException if an I/O error occurs during the update
     */
    @Override
    public Optional<Budget> update(LocalDate date, Budget updatedBudget) throws IOException {
        return update(budget -> budget.getDate().equals(date), updatedBudget);
    }

    /**
     * Checks if a budget exists for the given date.
     *
     * @param date the date to check
     * @return true if a budget exists; false otherwise
     * @throws IOException if an I/O error occurs during the check
     */
    @Override
    public boolean exists(LocalDate date) throws IOException {
        return super.exists(budget -> budget.getDate().equals(date));
    }

    /**
     * Counts the total number of budgets.
     *
     * @return the number of budgets
     * @throws IOException if an I/O error occurs while counting
     */
    @Override
    public long count() throws IOException {
        return super.count();
    }

    /**
     * Clears all budgets from the JSON file.
     *
     * @throws IOException if an I/O error occurs while clearing the data
     */
    @Override
    public void clear() throws IOException {
        super.clear();
    }
}
