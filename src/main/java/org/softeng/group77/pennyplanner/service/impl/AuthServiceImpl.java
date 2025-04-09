package org.softeng.group77.pennyplanner.service.impl;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.softeng.group77.pennyplanner.exception.RegistrationException;
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
    private static final Pattern CHINA_PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern UK_PHONE_PATTERN = Pattern.compile("^(?:\\+44|44|0)7\\d{9}$");
    private static final Pattern INTERNATIONAL_PHONE_PATTERN = Pattern.compile("^\\+?\\d{1,4}?[-.\\s]?\\(?\\d{1,4}?\\)?[-.\\s]?\\d{1," +
        "4}[-.\\s]?\\d{1,4}[-.\\s]?\\d{1,4}$");


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
    public User register(String username, String password, String email, String phone) {

        log.info("Registering user with username: {} and email: {}", username, email);

        if (StringUtils.isBlank(username) || StringUtils.isBlank(password) || StringUtils.isBlank(email) || StringUtils.isBlank(phone)) {
            throw new RegistrationException("All fields must be filled.");
        }

        Optional<User> existingUsername = userRepository.findByUsername(username.trim());
        if (existingUsername.isPresent()) {
            throw new RegistrationException("Username is already taken.");
        }

        Optional<User> existingEmail = userRepository.findByEmail(email.trim());
        if (existingEmail.isPresent()) {
            throw new RegistrationException("Email is already registered.");
        }

        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new RegistrationException("Invalid email format.");
        }

        if (!CHINA_PHONE_PATTERN.matcher(phone.trim()).matches()&&!UK_PHONE_PATTERN.matcher(phone.trim()).matches()&&!INTERNATIONAL_PHONE_PATTERN.matcher(phone.trim()).matches()) {
            throw new RegistrationException("Invalid phone number format.");
        }

        String passwordHash = passwordEncoder.encode(password);
        User newUser = new User(username.trim(), passwordHash, email.trim(), phone.trim());

        try {
            userRepository.save(newUser);
        } catch (Exception e) {
            log.error("Error while registering user: ", e);
            throw new RegistrationException("An error occurred during registration. Please try again later.");
        }
        log.info("User {}:{} registered successfully", newUser.getId(), newUser.getUsername());
        return newUser;
    }

}
