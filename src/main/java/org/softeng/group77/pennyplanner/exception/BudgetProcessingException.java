package org.softeng.group77.pennyplanner.exception;

/**
 * Exception thrown when a budget process occurs an error.
 *
 * @author MA Ruize
 * @version 2.0.0
 * @since 1.2.1
 */
public class BudgetProcessingException extends RuntimeException {
    public BudgetProcessingException(String message, Exception e) {
        super(message, e);
    }
}
