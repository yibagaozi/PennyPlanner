package org.softeng.group77.pennyplanner.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.softeng.group77.pennyplanner.dto.UserInfo;
import org.softeng.group77.pennyplanner.repository.UserRepository;
import org.softeng.group77.pennyplanner.service.AuthService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the authentication system in the PennyPlanner application.
 *
 * @author JIANG Mengnan
 * @version 2.0.0
 * @since 2.0.0
 */
@SpringBootTest
public class AuthenticationIntegrationTest {

    @Autowired
    private AuthService authService;


    /**
     * Sets up the test environment before each test method.
     * Ensures no user is logged in and resets the test context.
     */
    @BeforeEach
    public void setUp() {
        if (authService.isLoggedIn()) {
            authService.logout();
        }

        TestContext.reset();
    }

    /**
     * Tests the complete user registration and login flow.
     * The test uses random user information to ensure test isolation.
     *
     * @throws Exception if any authentication operations fail
     */
    @Test
    public void testUserRegistrationAndLogin() throws Exception {
        System.out.println("======== Start test: Login and Register ========");

        // Generate random user information
        String randomSuffix = UUID.randomUUID().toString().substring(0, 8);
        String username = "testuser" + randomSuffix;
        String password = "Password123";
        String email = "test" + randomSuffix + "@example.com";
        String phone = "13812345678";

        // 1. User registration
        UserInfo userInfo = authService.register(username, password, email, phone);

        // Verify successful registration
        assertNotNull(userInfo, "User information should not be null");
        assertNotNull(userInfo.getId(), "User ID should not be null");
        assertEquals(username, userInfo.getUsername(), "Username does not match");
        assertEquals(email, userInfo.getEmail(), "Email does not match");
        assertEquals(phone, userInfo.getPhone(), "Phone number does not match");

        System.out.println("Register success: " + userInfo.getUsername() + " (ID: " + userInfo.getId() + ")");

        // 2. User login
        UserInfo loginInfo = authService.login(username, password);

        // Verify successful login
        assertNotNull(loginInfo, "Login information should not be null");
        assertEquals(userInfo.getId(), loginInfo.getId(), "User ID after login does not match");
        assertTrue(authService.isLoggedIn(), "User should be in logged-in state");

        System.out.println("Log in success");

        // Save user information for subsequent tests
        TestContext.setUserId(loginInfo.getId());
        TestContext.setUsername(loginInfo.getUsername());
        TestContext.setPassword(password);

        System.out.println("======== Register and Login finished ========");
    }

    /**
     * Cleans up the test environment after each test method.
     * Ensures the user is logged out after each test.
     */
    @AfterEach
    public void tearDown() {
        // 确保每个测试结束后用户处于登出状态
        if (authService.isLoggedIn()) {
            authService.logout();
        }
    }
}