package org.softeng.group77.pennyplanner.exception;

/**
 * Exception thrown when a registration process occurs an error.
 *
 * @author MA Ruize
 * @version 2.0.0
 * @since 1.0.0
 */
public class RegistrationException extends RuntimeException {

    public RegistrationException(String message) {
        super(message);
    }

    public RegistrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public RegistrationException(Throwable cause) {
        super(cause);
    }

}
