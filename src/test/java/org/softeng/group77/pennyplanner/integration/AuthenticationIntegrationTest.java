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

@SpringBootTest
public class AuthenticationIntegrationTest {

    @Autowired
    private AuthService authService;

    @BeforeEach
    public void setUp() {
        if (authService.isLoggedIn()) {
            authService.logout();
        }

        TestContext.reset();
    }

    @Test
    public void testUserRegistrationAndLogin() throws Exception {
        System.out.println("======== 开始测试: 用户注册与登录 ========");

        // 生成随机用户信息
        String randomSuffix = UUID.randomUUID().toString().substring(0, 8);
        String username = "testuser" + randomSuffix;
        String password = "Password123";
        String email = "test" + randomSuffix + "@example.com";
        String phone = "13812345678";

        // 1. 用户注册
        UserInfo userInfo = authService.register(username, password, email, phone);

        // 验证注册成功
        assertNotNull(userInfo, "用户信息不应为空");
        assertNotNull(userInfo.getId(), "用户ID不应为空");
        assertEquals(username, userInfo.getUsername(), "用户名不匹配");
        assertEquals(email, userInfo.getEmail(), "邮箱不匹配");
        assertEquals(phone, userInfo.getPhone(), "手机号不匹配");

        System.out.println("用户注册成功: " + userInfo.getUsername() + " (ID: " + userInfo.getId() + ")");

        // 2. 用户登录
        UserInfo loginInfo = authService.login(username, password);

        // 验证登录成功
        assertNotNull(loginInfo, "登录信息不应为空");
        assertEquals(userInfo.getId(), loginInfo.getId(), "登录后的用户ID不匹配");
        assertTrue(authService.isLoggedIn(), "用户应该处于登录状态");

        System.out.println("用户登录成功");

        // 保存用户信息供后续测试使用
        TestContext.setUserId(loginInfo.getId());
        TestContext.setUsername(loginInfo.getUsername());
        TestContext.setPassword(password);

        System.out.println("======== 用户注册与登录测试完成 ========");
    }

    @AfterEach
    public void tearDown() {
        // 确保每个测试结束后用户处于登出状态
        if (authService.isLoggedIn()) {
            authService.logout();
        }
    }
}