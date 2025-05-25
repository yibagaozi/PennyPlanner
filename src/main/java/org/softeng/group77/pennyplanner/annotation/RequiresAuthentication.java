package org.softeng.group77.pennyplanner.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark methods that require user authentication before execution.
 * When this annotation is applied to a method, the system will verify that the user
 * is properly authenticated before allowing the method to execute.
 * Example usage:
 * <pre>
 * {@code
 * @RequiresAuthentication
 * public void securedMethod() {
 *     // Method that requires authentication
 * }
 * }
 * </pre>
 *
 * @author MA Ruize
 * @version 2.0.0
 * @since 1.1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresAuthentication {

    /**
     * Optional parameter that can specify a specific role or authentication level required.
     * When provided, the security system will check not only if the user is authenticated
     *
     * @return the required role or permission level, empty string if any authenticated user is allowed
     */
    String value() default "";
}
