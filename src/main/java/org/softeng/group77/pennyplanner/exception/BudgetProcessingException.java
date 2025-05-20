package org.softeng.group77.pennyplanner.exception;

public class BudgetProcessingException extends RuntimeException {
    public BudgetProcessingException(String message, Exception e) {
        super(message, e);
    }
}
