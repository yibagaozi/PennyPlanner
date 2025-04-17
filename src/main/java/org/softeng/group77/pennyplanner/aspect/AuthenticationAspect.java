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

@Aspect
@Component
@Order(1)
public class AuthenticationAspect {

    private final AuthService authService;

    public AuthenticationAspect(AuthService authService) {
        this.authService = authService;
    }

    @Pointcut("execution(* org.softeng.group77.pennyplanner.service.*Service.*(..))")
    public void serviceLayer() {}

    @Pointcut("!execution(* org.softeng.group77.pennyplanner.service.AuthService.*(..))")
    public void notAuthService() {}

    @Pointcut("@annotation(org.softeng.group77.pennyplanner.annotation.RequiresAuthentication)")
    public void requiresAuth() {}

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
