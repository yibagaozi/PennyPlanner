package org.softeng.group77.pennyplanner.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.concurrent.Task;  // 添加Task导入
import javafx.scene.control.Label;  // 添加Label导入
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import org.softeng.group77.pennyplanner.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.naming.AuthenticationException;
import java.io.IOException;

@Controller // 让 Spring 管理这个控制器
public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button continueButton;
    @FXML private Hyperlink createAccountLink;
    @FXML private Label errorLabel; // 用于显示错误信息

    @Autowired  // 确保authService被正确注入
    private AuthService authService;

    @FXML
    private void initialize() {

        createAccountLink.setOnAction(event -> {
            try {
                handleCreateAccount();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        continueButton.setOnAction(event -> {
            try {
                turntoHome();
            } catch (Exception e) {
                showError("登录失败: " + e.getMessage());
            }
        });
    }
    @FXML
    private void turntoHome() throws IOException {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Username and password must not be empty.");
            return;
        }

        // 这里可以添加验证逻辑
        System.out.println("尝试登录 - 邮箱/电话: " + email + ", 密码: " + password);
        System.out.println("跳转Home页面");
        MainApp.showHome();
        // 验证成功后导航到主界面

        // 使用 Task 进行异步登录（避免阻塞 UI）
        Task<Void> loginTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    authService.login(email, password); // 调用 AuthService 登录
                    Platform.runLater(() -> {
                        try {
                            MainApp.showHome(); // 登录成功，跳转主页
                        } catch (IOException e) {
                            showError("无法跳转到主页: " + e.getMessage());
                        }
                    });
                } catch (AuthenticationException e) {
                    showError(e.getMessage()); // 显示登录错误
                } catch (Exception e) {
                    showError("登录失败: " + e.getMessage());
                }
                return null;
            }
        };

        new Thread(loginTask).start(); // 启动后台任务
    }


    private void handleCreateAccount() throws IOException {
        System.out.println("跳转注册页面");
        MainApp.showSignup();
    }

    private void showError(String message) {
        Platform.runLater(() -> errorLabel.setText(message));
    }
    
}
