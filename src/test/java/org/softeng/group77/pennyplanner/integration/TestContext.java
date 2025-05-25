package org.softeng.group77.pennyplanner.integration;

import lombok.Getter;
import org.softeng.group77.pennyplanner.dto.UserInfo;
import org.softeng.group77.pennyplanner.service.AuthService;

import java.util.UUID;

/**
 * Test helper class that maintains authentication context for integration tests.
 * The context can be reset between tests to ensure isolation of test cases.
 *
 * @author JIANG Mengnan
 * @version 2.0.0
 * @since 2025-05-25
 */
public class TestContext {

    @Getter
    private static String userId;
    @Getter
    private static String username;
    @Getter
    private static String password;

    // 默认密码
    private static final String DEFAULT_PASSWORD = "Password123";

    // 是否已初始化
    private static boolean initialized = false;

    public static void setUserId(String userId) {
        TestContext.userId = userId;
    }

    public static void setUsername(String username) {
        TestContext.username = username;
    }

    public static void setPassword(String password) {
        TestContext.password = password;
    }

    /**
     * Ensures an authenticated user is available for testing.
     *
     * @param authService the authentication service to use
     * @return the authenticated user information
     * @throws Exception if user creation or authentication fails
     */
    public static synchronized UserInfo ensureAuthenticatedUser(AuthService authService) throws Exception {
        // 如果已经初始化但未登录状态，尝试登录
        if (initialized && !authService.isLoggedIn() && username != null) {
            try {
                return authService.login(username, password);
            } catch (Exception e) {
                // 如果登录失败，重置初始化状态
                System.out.println("Create new user: " + e.getMessage());
                initialized = false;
            }
        }

        // 如果未初始化，创建新用户
        if (!initialized) {
            // 创建随机用户名和邮箱
            String randomSuffix = UUID.randomUUID().toString().substring(0, 8);
            String newUsername = "testuser" + randomSuffix;
            String newPassword = DEFAULT_PASSWORD;
            String email = "test" + randomSuffix + "@example.com";
            String phone = "13812345678";

            try {
                // 注册用户
                UserInfo userInfo = authService.register(newUsername, newPassword, email, phone);

                // 登录用户
                UserInfo loginInfo = authService.login(newUsername, newPassword);

                // 保存认证信息
                userId = loginInfo.getId();
                username = loginInfo.getUsername();
                password = newPassword;

                initialized = true;
                System.out.println("User created: " + username);

                return loginInfo;
            } catch (Exception e) {
                System.out.println("Create failed: " + e.getMessage());
                throw e;
            }
        }

        // 返回当前登录用户信息
        return authService.getCurrentUser();
    }

    /**
     * Resets the test context.
     * Clears all stored user credentials and resets the initialization status.
     */
    public static void reset() {
        userId = null;
        username = null;
        password = null;
        initialized = false;
    }

    /**
     * Checks if a user is currently authenticated.
     *
     * @param authService the authentication service to check
     * @return true if a user is logged in, false otherwise
     */
    public static boolean isAuthenticated(AuthService authService) {
        return authService.isLoggedIn();
    }
}
