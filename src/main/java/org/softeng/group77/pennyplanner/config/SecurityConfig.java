package org.softeng.group77.pennyplanner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Security configuration for the PennyPlanner application.
 *
 * @author yibagaozi
 * @version 2.0.0
 * @deprecated This class is deprecated and will be removed in future versions.
 */
@Deprecated
@Configuration
public class SecurityConfig {

    /**
     * Creates password encoder bean for secure password handling.
     *
     * @return configured password encoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
