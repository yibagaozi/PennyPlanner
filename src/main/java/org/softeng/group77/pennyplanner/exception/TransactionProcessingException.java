package org.softeng.group77.pennyplanner.exception;

import java.io.IOException;

/**
 * Exception thrown when a transaction process occurs an error.
 *
 * @author MA Ruize
 * @version 2.0.0
 * @since 1.0.0
 */
public class TransactionProcessingException extends RuntimeException {
    public TransactionProcessingException(String message, Exception e) {
        super(message, e);
    }
}
