package org.softeng.group77.pennyplanner.service;

import org.softeng.group77.pennyplanner.model.Budget;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Provides budget management operations.
 * Handles creating, retrieving, and managing user budgets.
 *
 * @author Jiang Mengnan
 * @version 2.0.0
 * @since 1.1.0
 */
public interface BudgetService {
    /**
     * Creates or updates a budget for the specified date
     *
     * @param amount the budget amount
     * @param date the date for which to set the budget
     * @return the saved budget entity
     * @throws Exception if the budget cannot be saved
     */
    Budget saveBudget(double amount, LocalDate date) throws Exception;

    /**
     * Retrieves the budget for the current date
     *
     * @return the current budget
     * @throws Exception if the budget cannot be retrieved
     */
    Budget getCurrentBudget() throws Exception;

    /**
     * Retrieves the budget for a specific date
     *
     * @param date the date to get the budget for
     * @return the budget for the specified date
     * @throws Exception if the budget cannot be retrieved
     */
    Budget getBudgetByDate(LocalDate date) throws Exception;

    /**
     * Retrieves the budget for a specific month
     *
     * @param yearMonth the year and month to get the budget for
     * @return the budget for the specified month
     * @throws Exception if the budget cannot be retrieved
     */
    Budget getBudgetByYearMonth(YearMonth yearMonth) throws Exception;
}
