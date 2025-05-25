package org.softeng.group77.pennyplanner.integration;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.softeng.group77.pennyplanner.dto.UserInfo;
import org.softeng.group77.pennyplanner.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.naming.AuthenticationException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for user profile management in the PennyPlanner application.
 * The tests use the TestContext helper to ensure a consistent authenticated
 * state and to track credentials between test operations.
 *
 * @author JIANG Mengnan
 * @version 2.0.0
 * @since 2.0.0
 */
@SpringBootTest
public class UserProfileIntegrationTest {

    @Autowired
    private AuthService authService;

    /**
     * Sets up the test environment before each test method.
     * Ensures a test user is authenticated for the profile tests.
     *
     * @throws Exception if authentication fails
     */
    @BeforeEach
    public void setUp() throws Exception {
        // 确保用户已登录，必要时创建新用户
        TestContext.ensureAuthenticatedUser(authService);
    }

    /**
     * Tests the complete user profile update workflow.
     *
     * @throws Exception if any profile operations fail
     */
    @Test
    public void testUserProfileUpdate() throws Exception {
        System.out.println("======== Start test: User Profile Update ========");

        String userId = TestContext.getUserId();
        System.out.println("User ID: " + userId);

        // 1. 更新个人资料
        UserInfo updatedInfo = new UserInfo();
        updatedInfo.setUsername("penny_updated");
        updatedInfo.setEmail("updated@example.com");
        updatedInfo.setPhone("13900001111");

        UserInfo updatedUserInfo = authService.updateUserInfo(userId, updatedInfo);

        // 验证个人资料更新
        assertNotNull(updatedUserInfo, "Updated user information should not be null");
        assertEquals("penny_updated", updatedUserInfo.getUsername(), "Updated username does not match");
        assertEquals("updated@example.com", updatedUserInfo.getEmail(), "Updated email does not match");
        assertEquals("13900001111", updatedUserInfo.getPhone(), "Updated phone number does not match");

        System.out.println("Updated: " + updatedUserInfo.getUsername());

        // 更新全局上下文中的用户名
        TestContext.setUsername(updatedUserInfo.getUsername());

        // 2. 更改密码
        String oldPassword = TestContext.getPassword();
        String newPassword = "NewPassword123";
        UserInfo passwordChanged = authService.changePassword(userId, oldPassword, newPassword);

        // 验证密码更改
        assertNotNull(passwordChanged, "User profile not null");

        System.out.println("Password changed");

        // 3. 使用旧密码登录(应失败)
        authService.logout();
        assertThrows(AuthenticationException.class, () -> {
            authService.login(TestContext.getUsername(), oldPassword);
        }, "Old password login should fail");

        System.out.println("Old password login failed as expected");

        // 4. 使用新密码登录
        UserInfo reloginInfo = authService.login(TestContext.getUsername(), newPassword);

        // 验证使用新凭证登录
        assertNotNull(reloginInfo, "Relogin user information should not be null");
        assertEquals(userId, reloginInfo.getId(), "Relogin user ID does not match");
        assertTrue(authService.isLoggedIn(), "User should be logged in with new password");

        // 更新全局上下文中的密码
        TestContext.setPassword(newPassword);

        System.out.println("New password login success");
        System.out.println("======== User Profile Update Test Success ========");
    }
}
