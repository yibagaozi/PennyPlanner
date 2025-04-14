package org.softeng.group77.pennyplanner.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class SignupController {
    @FXML
    private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button signUpButton;
    @FXML private TextField phoneField;

    @FXML
    private void initialize() {
        // 初始化代码
    }

    @FXML
    private void handleSignUp() {
        System.out.println("Sign up with: " + emailField.getText());
        System.out.println("Password: " + passwordField.getText());
        // 添加实际注册逻辑
    }
    @FXML
    private void turntoLogin() throws IOException {
        System.out.println("Login");
        MainApp.showLogin();
    }
}
