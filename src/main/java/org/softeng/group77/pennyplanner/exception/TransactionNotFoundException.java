package org.softeng.group77.pennyplanner.exception;

/**
 * Exception thrown when a transaction cannot be found.
 *
 * @author MA Ruize
 * @version 2.0.0
 * @since 1.0.0
 */
public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(String message) {
        super(message);
    }
}
