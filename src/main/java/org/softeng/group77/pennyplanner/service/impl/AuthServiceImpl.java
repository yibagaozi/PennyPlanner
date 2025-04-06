package org.softeng.group77.pennyplanner.service.impl;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.softeng.group77.pennyplanner.model.User;
import org.softeng.group77.pennyplanner.repository.UserRepository;
import org.softeng.group77.pennyplanner.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private User currentUser;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.currentUser = null;
    }

    @Override
    public User login(String username, String password) throws AuthenticationException {

        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw new AuthenticationException("Username and password must not be empty.");
        }

        try {
            String usernameTrim = username.trim();
            String passwordHash = passwordEncoder.encode(password);
            Optional<User> userOpt;
            if (username.contains("@") && EMAIL_PATTERN.matcher(username).matches()) {
                userOpt = userRepository.findByEmail(usernameTrim);
            } else {
                userOpt = userRepository.findByUsername(usernameTrim);
            }

            if (userOpt.isEmpty()) {
                throw new AuthenticationException("Invalid username.");
            }

            currentUser = userOpt.get();
            log.info("User {} attempting to login", usernameTrim);

            if (!passwordEncoder.matches(passwordHash, currentUser.getPasswordHash())) {
                throw new AuthenticationException("Invalid username or password.");
            }

            currentUser.setLastLoginAt(LocalDateTime.now());

            return currentUser;
        } catch (Exception e) {
            log.error("Error while logging: ", e);
            throw new AuthenticationException("An error occurred during login. Please try again later.");
        }
    }

    @Override
    public User register(String username, String password, String email) {
        log.info("Registering user with username: {} and email: {}", username, email);

        String passwordHash = passwordEncoder.encode(password);
        User newUser = new User(null, passwordHash, null, null);
        log.info("User {} registered successfully", newUser.getId());
        return newUser;
    }

}
