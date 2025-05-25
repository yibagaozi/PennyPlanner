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

/**
 * Controller for the login screen in the PennyPlanner application.
 * This controller manages the user authentication process, including:
 * The controller uses Spring's dependency injection to obtain the
 * authentication service and performs asynchronous login operations
 * to maintain UI responsiveness.
 *
 * @author CHAI Jiayang
 * @version 2.0.0
 * @since 1.0.0
 */
@Controller // 让 Spring 管理这个控制器
public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button continueButton;
    @FXML private Hyperlink createAccountLink;
    @FXML private Label errorLabel; // 用于显示错误信息

    @Autowired  // 确保authService被正确注入
    private AuthService authService;

    /**
     * Initializes the controller after FXML elements are loaded.
     * Sets up event handlers for UI components such as the login button
     * and create account link.
     */
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
                Platform.runLater(() -> errorLabel.setText("登录失败: " + e.getMessage()));
            }
        });
    }

    /**
     * Handles the user login process.
     * This method validates that username and password fields are not empty,
     * then attempts to authenticate the user through the AuthService.
     * The authentication process runs in a background thread to avoid blocking
     * the UI thread. On successful login, the user is redirected to the home screen.
     * On failure, an appropriate error message is displayed.
     */
    @FXML
    private void turntoHome() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            Platform.runLater(() -> errorLabel.setText("Username and password must not be empty."));
            return;
        }

        // 禁用登录按钮，显示加载状态
        continueButton.setDisable(true);
        errorLabel.setText("Logging in...");

        // 使用 Task 进行异步登录（避免阻塞 UI）
        Task<Void> loginTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    authService.login(email, password); // 调用 AuthService 登录
                    Platform.runLater(() -> {
                        try {
                            // 登录成功后刷新事务数据
                            SharedDataModel.refreshTransactionData();
                            MainApp.showHome(); // 登录成功，跳转主页
                        } catch (IOException e) {
                            String errorMsg = "跳转主页失败: " + e.getMessage();
                            System.err.println(errorMsg);
                            e.printStackTrace();
                            throw new RuntimeException("跳转主页失败: " + e.getMessage());
                            //Platform.runLater(() -> errorLabel.setText("无法跳转到主页: " + e.getMessage()));
                        }
                    });
                } catch (AuthenticationException e) {
                    Platform.runLater(() -> errorLabel.setText(e.getMessage())); // 显示登录错误
                } catch (Exception e) {
                    Platform.runLater(() -> errorLabel.setText("登录失败: " + e.getMessage()));
                    System.out.println(e.getMessage());
                }finally {
                    // 无论成功失败，重新启用登录按钮
                    Platform.runLater(() -> continueButton.setDisable(false));
                }
                return null;
            }
        };

        new Thread(loginTask).start(); // 启动后台任务
    }

    /**
     * Navigates to the account creation screen.
     *
     * @throws IOException if navigation to the signup screen fails
     */
    private void handleCreateAccount() throws IOException {
        System.out.println("跳转注册页面");
        MainApp.showSignup();
    }

    /**
     * Displays an error message in the UI.
     *
     * @param message the error message to display
     */
    private void showError(String message) {
        Platform.runLater(() -> errorLabel.setText(message));
    }
    
}
