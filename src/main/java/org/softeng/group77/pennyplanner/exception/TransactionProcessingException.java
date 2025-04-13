package org.softeng.group77.pennyplanner.exception;

import java.io.IOException;

public class TransactionProcessingException extends RuntimeException {
    public TransactionProcessingException(String message, Exception e) {
        super(message);
    }
}
