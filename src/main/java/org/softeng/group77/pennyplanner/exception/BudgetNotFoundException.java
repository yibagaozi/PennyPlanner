package org.softeng.group77.pennyplanner.exception;

/**
 * Exception thrown when a budget cannot be found.
 *
 * @author MA Ruize
 * @version 2.0.0
 * @since 1.2.1
 */
public class BudgetNotFoundException extends RuntimeException {
    public BudgetNotFoundException(String message) {
        super(message);
    }
}
