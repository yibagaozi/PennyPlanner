package org.softeng.group77.pennyplanner.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;  // 添加Label导入
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

import org.softeng.group77.pennyplanner.dto.UserInfo;
import org.softeng.group77.pennyplanner.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class SignupController {
    @FXML
    private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button signUpButton;
    @FXML private TextField phoneField;
    @FXML private Label errorLabel; // 用于显示错误信息

    @Autowired
    private AuthService authService;

    @FXML
    private void initialize() {
        signUpButton.setOnAction(event -> {
            handleSignUp();
        });
    }

    @FXML
    private void handleSignUp() {
        String username = emailField.getText(); // 假设用户名为邮箱
        String password = passwordField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            errorLabel.setText("All fields must be filled.");
            return;
    }
        try {
            UserInfo userInfo = authService.register(username, password, email, phone);
            if (userInfo != null) {
                errorLabel.setText("Registration successful!");
                turntoLogin();
            } else {
                errorLabel.setText("Registration failed.");
            }
        } catch (Exception e) {
            errorLabel.setText("Registration failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    private void turntoLogin() throws IOException {
        System.out.println("Login");
        MainApp.showLogin();
    }
}
