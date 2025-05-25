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

/**
 * Controller for the user registration screen in the PennyPlanner application.
 * The controller works with a form that contains fields for username, email,
 * phone number, and password, along with a signup button and error display area.
 *
 * @author CHAI Jiayang
 * @version 2.0.0
 * @since 1.0.0
 */
@Controller
public class SignupController {
    @FXML
    private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button signUpButton;
    @FXML private TextField usernameField;
    @FXML private TextField phoneField;
    @FXML private Label errorLabel; // 用于显示错误信息

    @Autowired
    private AuthService authService;

    /**
     * Initializes the controller after FXML elements are loaded.
     * Sets up action handlers for the signup button.
     */
    @FXML
    private void initialize() {
        signUpButton.setOnAction(event -> {
            handleSignUp();
        });
    }

    /**
     * Processes the user registration form submission.
     */
    @FXML
    private void handleSignUp() {
        String username = usernameField.getText(); // 假设用户名为邮箱
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

    /**
     * Navigates to the login screen.
     *
     * @throws IOException if navigation to the login screen fails
     */
    @FXML
    private void turntoLogin() throws IOException {
        System.out.println("Login");
        MainApp.showLogin();
    }
}
