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

    @BeforeEach
    void setUp() {
        testUser = new User(testUsername, testPasswordHash, testEmail, testPhone);
        testUser.setLastLoginAt(LocalDateTime.now());
    }

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

    @Test
    @DisplayName("Empty field login")
    void login_withEmptyCredentials_shouldThrowException() {
        assertThrows(AuthenticationException.class, () -> authService.login("", testPassword),
                "Empty username login");

        assertThrows(AuthenticationException.class, () -> authService.login(testUsername, ""),
                "Empty password login");
    }

    @Test
    @DisplayName("Invalid username login")
    void login_withInvalidUsername_shouldThrowException() throws IOException {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class, () -> authService.login("invalid", testPassword),
                "Invalid username login");
    }

    @Test
    @DisplayName("Wrong password login")
    void login_withInvalidPassword_shouldThrowException() throws IOException {
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), eq(testPasswordHash))).thenReturn(false);

        assertThrows(AuthenticationException.class, () -> authService.login(testUsername, "wrong_password"),
                "Wrong password login");
    }

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

    @Test
    @DisplayName("Empty field for register")
    void register_withEmptyFields_shouldThrowException() {
        assertThrows(RegistrationException.class, () -> authService.register("", testPassword, testEmail, testPhone), "Empty username for register");
        assertThrows(RegistrationException.class, () -> authService.register(testUsername, "", testEmail, testPhone), "Empty password");
        assertThrows(RegistrationException.class, () -> authService.register(testUsername, testPassword, "", testPhone), "Empty email");
        assertThrows(RegistrationException.class, () -> authService.register(testUsername, testPassword, testEmail, ""), "Empty phone");
    }

    @Test
    @DisplayName("Username exists for register")
    void register_withExistingUsername_shouldThrowException() throws IOException {
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));

        assertThrows(RegistrationException.class, () -> authService.register(testUsername, testPassword, testEmail, testPhone),
            "Username exists for register");
    }

    @Test
    @DisplayName("Email exists for register")
    void register_withExistingEmail_shouldThrowException() throws IOException {
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        assertThrows(RegistrationException.class, () -> authService.register(testUsername, testPassword, testEmail, testPhone), "Email exists for register");
    }

    @Test
    @DisplayName("Invalid email for register")
    void register_withInvalidEmail_shouldThrowException() throws IOException {
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(RegistrationException.class, () -> authService.register(testUsername, testPassword, "invalid-email", testPhone),
            "Invalid email for register");
    }

    @Test
    @DisplayName("Invalid phone for register")
    void register_withInvalidPhone_shouldThrowException() throws IOException {
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());
      
        assertThrows(RegistrationException.class, () -> authService.register(testUsername, testPassword, testEmail, "invalid-phone"),
            "Invalid phone for register");
    }

    @Test
    @DisplayName("Clean cache after logged out")
    void logout_shouldClearUserCache() throws AuthenticationException, IOException {
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(testPassword, testPasswordHash)).thenReturn(true);
        authService.login(testUsername, testPassword);

        authService.logout();

        assertFalse(authService.isLoggedIn(), "Logged out");
    }

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
