package org.softeng.group77.pennyplanner.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.softeng.group77.pennyplanner.dto.UserInfo;
import org.softeng.group77.pennyplanner.exception.RegistrationException;
import org.softeng.group77.pennyplanner.model.User;
import org.softeng.group77.pennyplanner.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
/**
 * Unit tests for the AuthServiceImpl class.
 * These tests verify the authentication, registration, and password management
 * functionality provided by the AuthServiceImpl class. The tests use Mockito
 * to mock the dependencies (UserRepository and PasswordEncoder) and focus on
 * testing the service's business logic in isolation.
 *
 * @author XI Yu
 * @version 2.0.0
 * @since 1.1.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private final String testUsername = "testuser";
    private final String testEmail = "test@example.com";
    private final String testPhone = "13912345678";
    private final String testPassword = "password123";
    private final String testPasswordHash = "hashedPassword";

    /**
     * Sets up the test environment before each test method.
     * Creates a test user with predefined credentials.
     */
    @BeforeEach
    void setUp() {
        testUser = new User(testUsername, testPasswordHash, testEmail, testPhone);
        testUser.setLastLoginAt(LocalDateTime.now());
    }

    /**
     * Tests that a user can successfully log in using a valid username and password.
     * Verifies that the correct user information is returned after login.
     *
     * @throws AuthenticationException if authentication fails
     * @throws IOException if an I/O error occurs
     */
    @Test
    @DisplayName("Valid username login")
    void login_withValidUsername_shouldReturnUserInfo() throws AuthenticationException, IOException {
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(testPassword, testPasswordHash)).thenReturn(true);

        UserInfo result = authService.login(testUsername, testPassword);

        assertNotNull(result, "No empty user info");
        assertEquals(testUsername, result.getUsername(), "Username match");
        assertEquals(testEmail, result.getEmail(), "Email match");
        verify(userRepository).findByUsername(testUsername);
    }

    /**
     * Tests that a user can successfully log in using a valid email and password.
     * Verifies that the correct user information is returned after login.
     *
     * @throws AuthenticationException if authentication fails
     * @throws IOException if an I/O error occurs
     */
    @Test
    @DisplayName("Valid email login")
    void login_withValidEmail_shouldReturnUserInfo() throws AuthenticationException, IOException {
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(testPassword, testPasswordHash)).thenReturn(true);

        UserInfo result = authService.login(testEmail, testPassword);

        assertNotNull(result, "No empty user info");
        assertEquals(testUsername, result.getUsername(), "Username match");
        verify(userRepository).findByEmail(testEmail);
    }

    /**
     * Tests that login fails when empty credentials are provided.
     * Verifies that an AuthenticationException is thrown for empty username or password.
     */
    @Test
    @DisplayName("Empty field login")
    void login_withEmptyCredentials_shouldThrowException() {
        assertThrows(AuthenticationException.class, () -> authService.login("", testPassword),
                "Empty username login");

        assertThrows(AuthenticationException.class, () -> authService.login(testUsername, ""),
                "Empty password login");
    }

    /**
     * Tests that login fails when an invalid username is provided.
     * Verifies that an AuthenticationException is thrown when the user cannot be found.
     *
     * @throws IOException if an I/O error occurs
     */
    @Test
    @DisplayName("Invalid username login")
    void login_withInvalidUsername_shouldThrowException() throws IOException {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class, () -> authService.login("invalid", testPassword),
                "Invalid username login");
    }

    /**
     * Tests that login fails when an incorrect password is provided.
     * Verifies that an AuthenticationException is thrown when the password doesn't match.
     *
     * @throws IOException if an I/O error occurs
     */
    @Test
    @DisplayName("Wrong password login")
    void login_withInvalidPassword_shouldThrowException() throws IOException {
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), eq(testPasswordHash))).thenReturn(false);

        assertThrows(AuthenticationException.class, () -> authService.login(testUsername, "wrong_password"),
                "Wrong password login");
    }

    /**
     * Tests that a user can be successfully registered with valid details.
     * Verifies that the correct user information is returned after registration.
     *
     * @throws IOException if an I/O error occurs
     */
    @Test
    @DisplayName("Valid user info for register")
    void register_withValidDetails_shouldCreateUser() throws IOException {
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(testPassword)).thenReturn(testPasswordHash);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserInfo result = authService.register(testUsername, testPassword, testEmail, testPhone);

        assertNotNull(result, "No empty user info");
        assertEquals(testUsername, result.getUsername(), "Username match");
        assertEquals(testEmail, result.getEmail(), "Email match");
        verify(userRepository).save(any(User.class));
    }

    /**
     * Tests that registration fails when empty fields are provided.
     * Verifies that a RegistrationException is thrown for any empty required field.
     */
    @Test
    @DisplayName("Empty field for register")
    void register_withEmptyFields_shouldThrowException() {
        assertThrows(RegistrationException.class, () -> authService.register("", testPassword, testEmail, testPhone), "Empty username for register");
        assertThrows(RegistrationException.class, () -> authService.register(testUsername, "", testEmail, testPhone), "Empty password");
        assertThrows(RegistrationException.class, () -> authService.register(testUsername, testPassword, "", testPhone), "Empty email");
        assertThrows(RegistrationException.class, () -> authService.register(testUsername, testPassword, testEmail, ""), "Empty phone");
    }

    /**
     * Tests that registration fails when the username already exists.
     * Verifies that a RegistrationException is thrown for duplicate username.
     *
     * @throws IOException if an I/O error occurs
     */
    @Test
    @DisplayName("Username exists for register")
    void register_withExistingUsername_shouldThrowException() throws IOException {
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));

        assertThrows(RegistrationException.class, () -> authService.register(testUsername, testPassword, testEmail, testPhone),
            "Username exists for register");
    }

    /**
     * Tests that registration fails when the email already exists.
     * Verifies that a RegistrationException is thrown for duplicate email.
     *
     * @throws IOException if an I/O error occurs
     */
    @Test
    @DisplayName("Email exists for register")
    void register_withExistingEmail_shouldThrowException() throws IOException {
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        assertThrows(RegistrationException.class, () -> authService.register(testUsername, testPassword, testEmail, testPhone), "Email exists for register");
    }

    /**
     * Tests that registration fails when an invalid email format is provided.
     * Verifies that a RegistrationException is thrown for invalid email.
     *
     * @throws IOException if an I/O error occurs
     */
    @Test
    @DisplayName("Invalid email for register")
    void register_withInvalidEmail_shouldThrowException() throws IOException {
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(RegistrationException.class, () -> authService.register(testUsername, testPassword, "invalid-email", testPhone),
            "Invalid email for register");
    }

    /**
     * Tests that registration fails when an invalid phone format is provided.
     * Verifies that a RegistrationException is thrown for invalid phone.
     *
     * @throws IOException if an I/O error occurs
     */
    @Test
    @DisplayName("Invalid phone for register")
    void register_withInvalidPhone_shouldThrowException() throws IOException {
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());
      
        assertThrows(RegistrationException.class, () -> authService.register(testUsername, testPassword, testEmail, "invalid-phone"),
            "Invalid phone for register");
    }

    /**
     * Tests that the user cache is cleared after logout.
     * Verifies that isLoggedIn returns false after logout.
     *
     * @throws AuthenticationException if authentication fails
     * @throws IOException if an I/O error occurs
     */
    @Test
    @DisplayName("Clean cache after logged out")
    void logout_shouldClearUserCache() throws AuthenticationException, IOException {
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(testPassword, testPasswordHash)).thenReturn(true);
        authService.login(testUsername, testPassword);

        authService.logout();

        assertFalse(authService.isLoggedIn(), "Logged out");
    }

    /**
     * Tests that password change fails when the old password is incorrect.
     * Verifies that an AuthenticationException is thrown for invalid old password.
     *
     * @throws AuthenticationException if authentication fails
     * @throws IOException if an I/O error occurs
     */
    @Test
    @DisplayName("Wrong old password")
    void changePassword_withInvalidOldPassword_shouldThrowException() throws AuthenticationException, IOException {
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(testPassword, testPasswordHash)).thenReturn(true);
        authService.login(testUsername, testPassword);

        String wrongOldPassword = "wrongPassword";
        String newPassword = "newPassword123";
        when(userRepository.findById(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(wrongOldPassword, testPasswordHash)).thenReturn(false);

        assertThrows(AuthenticationException.class, () -> authService.changePassword(testUser.getId(), wrongOldPassword, newPassword),
            "Wrong old password");
    }
}
