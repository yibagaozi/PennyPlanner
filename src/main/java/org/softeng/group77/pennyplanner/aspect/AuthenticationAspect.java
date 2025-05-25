package org.softeng.group77.pennyplanner.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.softeng.group77.pennyplanner.dto.UserInfo;
import org.softeng.group77.pennyplanner.service.AuthService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.naming.AuthenticationException;
import java.lang.reflect.Method;

/**
 * Aspect class that enforces authentication requirements across the application's service layer.
 * This aspect intercepts method calls to service methods annotated with {@code @RequiresAuthentication}
 * and verifies that a user is properly authenticated before allowing the method execution to proceed.
 * If authentication fails, the aspect throws an AuthenticationException with details about
 * which method required authentication.
 *
 * @author MA Ruize
 * @version 2.0.0
 * @since 1.1.0
 * @see org.softeng.group77.pennyplanner.annotation.RequiresAuthentication
 */
@Aspect
@Component
@Order(1)
public class AuthenticationAspect {

    /**
     * The authentication service used to get and verify user authentication status.
     */
    private final AuthService authService;

    /**
     * Constructs a new AuthenticationAspect with the specified authentication service.
     *
     * @param authService the service to use for authentication verification
     */
    public AuthenticationAspect(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Pointcut that targets all methods in any service class within the application.
     */
    @Pointcut("execution(* org.softeng.group77.pennyplanner.service.*Service.*(..))")
    public void serviceLayer() {}

    /**
     * Pointcut that excludes all methods in the AuthService class.
     */
    @Pointcut("!execution(* org.softeng.group77.pennyplanner.service.AuthService.*(..))")
    public void notAuthService() {}

    /**
     * Pointcut that targets methods annotated with the RequiresAuthentication annotation.
     */
    @Pointcut("@annotation(org.softeng.group77.pennyplanner.annotation.RequiresAuthentication)")
    public void requiresAuth() {}

    /**
     * Advice that runs before the execution of methods matching the combined pointcuts.
     *
     * @param joinPoint the join point representing the intercepted method call
     * @throws AuthenticationException if no user is currently authenticated
     */
    @Before("serviceLayer() && notAuthService() && requiresAuth()")
    public void validateAuthentication(JoinPoint joinPoint) throws AuthenticationException {
        UserInfo currentUser = authService.getCurrentUser();

        if (currentUser == null) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            String methodName = method.getDeclaringClass().getSimpleName() + "." + method.getName();

            throw new AuthenticationException("Authentication required for: " + methodName);
        }
    }

}
