package org.softeng.group77.pennyplanner.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button continueButton;
    @FXML private Hyperlink createAccountLink;

    @FXML
    private void initialize() {

        createAccountLink.setOnAction(event -> {
            try {
                handleCreateAccount();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    @FXML
    private void turntoHome() throws IOException {
        String email = emailField.getText();
        String password = passwordField.getText();

        // 这里可以添加验证逻辑
        System.out.println("尝试登录 - 邮箱/电话: " + email + ", 密码: " + password);
        System.out.println("跳转Home页面");
        MainApp.showHome();
        // 验证成功后可以导航到主界面
    }

    private void handleCreateAccount() throws IOException {
        System.out.println("跳转注册页面");
        MainApp.showSignup();
    }
}
