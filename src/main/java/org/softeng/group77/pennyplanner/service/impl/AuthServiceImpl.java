package org.softeng.group77.pennyplanner.service.impl;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.softeng.group77.pennyplanner.dto.UserInfo;
import org.softeng.group77.pennyplanner.exception.RegistrationException;
import org.softeng.group77.pennyplanner.model.User;
import org.softeng.group77.pennyplanner.repository.UserRepository;
import org.softeng.group77.pennyplanner.service.AuthService;
import org.softeng.group77.pennyplanner.util.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.time.Duration;
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

    private UserInfo cachedUser;
    private LocalDateTime cacheTimestamp;
    private final Duration cacheExpiration = Duration.ofHours(144);

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
    public UserInfo login(String username, String password) throws AuthenticationException {

        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw new AuthenticationException("Username and password must not be empty.");
        }

        try {
            String usernameTrim = username.trim();
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

            if (!passwordEncoder.matches(password, currentUser.getPasswordHash())) {
                throw new AuthenticationException("Invalid password.");
            }

            currentUser.setLastLoginAt(LocalDateTime.now());

            UserInfo userInfo = new UserInfo(currentUser);
            updateUserCache(userInfo);

            return userInfo;
        } catch (Exception e) {
            log.error("Error while logging: ", e);
            throw new AuthenticationException("An error occurred during login. Please try again later.");
        }
    }

    @Override
    public UserInfo register(String username, String password, String email, String phone) throws IOException {

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
        return UserMapper.toUserInfo(newUser);
    }

    @Override
    public UserInfo getCurrentUser() {
        if (isUserCacheValid()) {
            log.info("Get current user: {}", cachedUser);
            return cachedUser;
        }

        clearUserCache();
        return null;
    }

    @Override
    public UserInfo updateUserInfo(String userId, UserInfo updatedInfo) throws RegistrationException, IOException {
        if (StringUtils.isBlank(userId) || updatedInfo == null) {
            log.warn("Attempted to update user info with blank userId or null updatedInfo.");
            return null;
        }
        // Assuming userRepository.findById() can take a String ID as per changePassword method.
        // If User entity's ID is Long, you'd need: Optional<User> userOpt = userRepository.findById(Long.valueOf(userId));
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            log.warn("User with ID {} not found for update.", userId);
            return null; // Or throw a specific UserNotFoundException
        }

        User userToUpdate = userOpt.get();
        log.info("Updating user info for user ID: {}", userId);

        boolean modified = false;

        // Update username
        if (StringUtils.isNotBlank(updatedInfo.getUsername()) && !userToUpdate.getUsername().equals(updatedInfo.getUsername().trim())) {
            // If username needs to be unique and can be changed, add uniqueness check:
            // Optional<User> existingUsername = userRepository.findByUsername(updatedInfo.getUsername().trim());
            // if (existingUsername.isPresent() && !existingUsername.get().getId().equals(userToUpdate.getId())) {
            //     throw new RegistrationException("Username is already taken by another user.");
            // }
            userToUpdate.setUsername(updatedInfo.getUsername().trim());
            modified = true;
            log.debug("Updating username for user ID: {}", userId);
        }

        // Update email
        if (StringUtils.isNotBlank(updatedInfo.getEmail()) && !userToUpdate.getEmail().equals(updatedInfo.getEmail().trim())) {
            String newEmailTrim = updatedInfo.getEmail().trim();
            if (!EMAIL_PATTERN.matcher(newEmailTrim).matches()) {
                throw new RegistrationException("Invalid new email format.");
            }
            Optional<User> existingEmailUser = userRepository.findByEmail(newEmailTrim);
            if (existingEmailUser.isPresent() && !existingEmailUser.get().getId().equals(userToUpdate.getId())) {
                // The new email is already registered to a DIFFERENT user.
                throw new RegistrationException("Email is already registered by another user.");
            }
            userToUpdate.setEmail(newEmailTrim);
            modified = true;
            log.debug("Updating email for user ID: {}", userId);
        }

        // Update phone
        if (StringUtils.isNotBlank(updatedInfo.getPhone()) && !userToUpdate.getPhone().equals(updatedInfo.getPhone().trim())) {
            String newPhoneTrim = updatedInfo.getPhone().trim();
            if (!CHINA_PHONE_PATTERN.matcher(newPhoneTrim).matches() && !UK_PHONE_PATTERN.matcher(newPhoneTrim).matches() && !INTERNATIONAL_PHONE_PATTERN.matcher(newPhoneTrim).matches()) {
                throw new RegistrationException("Invalid new phone number format.");
            }
            userToUpdate.setPhone(newPhoneTrim);
            modified = true;
            log.debug("Updating phone for user ID: {}", userId);
        }

        if (modified) {
            try {
                // userToUpdate.setUpdatedAt(LocalDateTime.now()); // If you have an updatedAt field
                User savedUser = userRepository.save(userToUpdate);
                UserInfo newUserInfo = UserMapper.toUserInfo(savedUser);

                // Important: Update cache only if the updated user is the currently cached user
                if (cachedUser != null && cachedUser.getId().equals(savedUser.getId().toString())) {
                    updateUserCache(newUserInfo);
                }
                log.info("User info updated successfully for user ID: {}", userId);
                return newUserInfo;
            } catch (Exception e) {
                log.error("Error while saving updated user info for ID {}: ", userId, e);
                // Depending on the exception, you might want to throw a more specific one.
                // For now, rethrowing as a generic runtime exception or returning null.
                // throw new RuntimeException("Failed to save updated user information.", e);
                return null; // Or throw custom exception
            }
        } else {
            log.info("No changes detected for user ID: {}. No update performed.", userId);
            return UserMapper.toUserInfo(userToUpdate); // Return current info if no changes
        }
    }

    @Override
    public UserInfo changePassword(String userId, String oldPassword, String newPassword) throws AuthenticationException {
        UserInfo currentUserInfo = getCurrentUser();
        if (currentUserInfo == null) {
            throw new AuthenticationException("User not logged in.");
        }

        try {
            Optional<User> userOpt = userRepository.findById(currentUserInfo.getId());

            if (userOpt.isEmpty()) {
                throw new AuthenticationException("Invalid username.");
            }

            User currentUser = userOpt.get();

            log.info("User {} is trying to change password: ", currentUser.getUsername());

            if (!passwordEncoder.matches(oldPassword, currentUser.getPasswordHash())) {
                log.info("User {} change password failed: wrong old password.", currentUser.getUsername());
                throw new AuthenticationException("Invalid old password.");
            }

            currentUser.setPasswordHash(passwordEncoder.encode(newPassword));
            userRepository.save(currentUser);

            log.info("User {} change password successfully.", currentUser.getUsername());

            UserInfo updatedInfo = new UserInfo(currentUser);
            updateUserCache(updatedInfo);

            return updatedInfo;
        } catch (Exception e) {
            log.error("Error while changing password: ", e);
            throw new AuthenticationException("An error occurred while changing password. Please try again later.");
        }
    }

    public void logout(){
        log.info("User {} logged out successfully.", cachedUser.getUsername());
        clearUserCache();
    }

    public boolean isLoggedIn(){
        return isUserCacheValid();
    }

    private void updateUserCache(UserInfo userInfo) {
        this.cachedUser = userInfo;
        this.cacheTimestamp = LocalDateTime.now();
    }

    private void clearUserCache() {
        this.cachedUser = null;
        this.cacheTimestamp = null;
    }

    private boolean isUserCacheValid() {
        if (cachedUser == null || cacheTimestamp == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        Duration elapsed = Duration.between(cacheTimestamp, now);
        return elapsed.compareTo(cacheExpiration) < 0;
    }

}
