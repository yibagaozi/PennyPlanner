package org.softeng.group77.pennyplanner.integration;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.softeng.group77.pennyplanner.dto.UserInfo;
import org.softeng.group77.pennyplanner.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.naming.AuthenticationException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserProfileIntegrationTest {

    @Autowired
    private AuthService authService;

    @BeforeEach
    public void setUp() throws Exception {
        // 确保用户已登录，必要时创建新用户
        TestContext.ensureAuthenticatedUser(authService);
    }

    /**
     * 测试用户信息更新流程
     */
    @Test
    public void testUserProfileUpdate() throws Exception {
        System.out.println("======== 开始测试: 用户信息更新 ========");

        String userId = TestContext.getUserId();
        System.out.println("使用用户ID: " + userId);

        // 1. 更新个人资料
        UserInfo updatedInfo = new UserInfo();
        updatedInfo.setUsername("penny_updated");
        updatedInfo.setEmail("updated@example.com");
        updatedInfo.setPhone("13900001111");

        UserInfo updatedUserInfo = authService.updateUserInfo(userId, updatedInfo);

        // 验证个人资料更新
        assertNotNull(updatedUserInfo, "更新后的用户信息不应为空");
        assertEquals("penny_updated", updatedUserInfo.getUsername(), "更新后的用户名不匹配");
        assertEquals("updated@example.com", updatedUserInfo.getEmail(), "更新后的邮箱不匹配");
        assertEquals("13900001111", updatedUserInfo.getPhone(), "更新后的手机号不匹配");

        System.out.println("个人资料更新成功: " + updatedUserInfo.getUsername());

        // 更新全局上下文中的用户名
        TestContext.setUsername(updatedUserInfo.getUsername());

        // 2. 更改密码
        String oldPassword = TestContext.getPassword();
        String newPassword = "NewPassword123";
        UserInfo passwordChanged = authService.changePassword(userId, oldPassword, newPassword);

        // 验证密码更改
        assertNotNull(passwordChanged, "密码更改后的用户信息不应为空");

        System.out.println("密码更改成功");

        // 3. 使用旧密码登录(应失败)
        authService.logout();
        assertThrows(AuthenticationException.class, () -> {
            authService.login(TestContext.getUsername(), oldPassword);
        }, "使用旧密码登录应该失败");

        System.out.println("使用旧密码登录失败验证成功");

        // 4. 使用新密码登录
        UserInfo reloginInfo = authService.login(TestContext.getUsername(), newPassword);

        // 验证使用新凭证登录
        assertNotNull(reloginInfo, "使用新凭证登录后的信息不应为空");
        assertEquals(userId, reloginInfo.getId(), "登录后的用户ID不匹配");
        assertTrue(authService.isLoggedIn(), "用户应该处于登录状态");

        // 更新全局上下文中的密码
        TestContext.setPassword(newPassword);

        System.out.println("使用新密码登录成功");
        System.out.println("======== 用户信息更新测试完成 ========");
    }
}
