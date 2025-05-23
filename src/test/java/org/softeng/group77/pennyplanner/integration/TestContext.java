package org.softeng.group77.pennyplanner.integration;

import lombok.Getter;
import org.softeng.group77.pennyplanner.dto.UserInfo;
import org.softeng.group77.pennyplanner.service.AuthService;

import java.util.UUID;

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
     * 确保用户已登录，如果没有则创建并登录一个新用户
     * @param authService 认证服务
     * @return 登录的用户信息
     */
    public static synchronized UserInfo ensureAuthenticatedUser(AuthService authService) throws Exception {
        // 如果已经初始化但未登录状态，尝试登录
        if (initialized && !authService.isLoggedIn() && username != null) {
            try {
                return authService.login(username, password);
            } catch (Exception e) {
                // 如果登录失败，重置初始化状态
                System.out.println("无法使用现有凭据登录，正在创建新用户: " + e.getMessage());
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
                System.out.println("测试用户已创建和登录: " + username);

                return loginInfo;
            } catch (Exception e) {
                System.out.println("创建测试用户失败: " + e.getMessage());
                throw e;
            }
        }

        // 返回当前登录用户信息
        return authService.getCurrentUser();
    }

    /**
     * 重置测试上下文
     */
    public static void reset() {
        userId = null;
        username = null;
        password = null;
        initialized = false;
    }

    /**
     * 检查用户是否已登录
     */
    public static boolean isAuthenticated(AuthService authService) {
        return authService.isLoggedIn();
    }
}
