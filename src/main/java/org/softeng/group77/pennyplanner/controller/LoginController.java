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
 * LoginController 负责处理用户登录逻辑，包括验证用户凭据，显示错误信息和跳转页面。
 * 该控制器使用 AuthService 进行身份验证，并处理登录过程中的 UI 更新。
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
     * 初始化方法，设置界面上的事件处理器。
     * 包括绑定注册页面跳转、登录按钮点击等行为。
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
     * 处理用户登录逻辑，验证用户名和密码是否为空，调用 AuthService 进行登录。
     * 登录过程是异步的，通过 Task 来避免阻塞 UI 线程。
     * 如果登录成功，跳转到主页；如果失败，显示相应的错误信息。
     * 
     * @throws IOException 如果页面跳转失败
     */
    @FXML
    private void turntoHome() throws IOException {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            Platform.runLater(() -> errorLabel.setText("Username and password must not be empty."));
            return;
        }

        // 禁用登录按钮，显示加载状态
        continueButton.setDisable(true);
        errorLabel.setText("登录中...");

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
                            Platform.runLater(() -> errorLabel.setText("无法跳转到主页: " + e.getMessage()));
                        }
                    });
                } catch (AuthenticationException e) {
                    Platform.runLater(() -> errorLabel.setText(e.getMessage())); // 显示登录错误
                } catch (Exception e) {
                    Platform.runLater(() -> errorLabel.setText("登录失败: " + e.getMessage()));
                    System.out.println(e.getMessage());
                } finally {
                    // 无论成功失败，重新启用登录按钮
                    Platform.runLater(() -> continueButton.setDisable(false));
                }
                return null;
            }
        };

        new Thread(loginTask).start(); // 启动后台任务
    }

    /**
     * 跳转到创建账户页面。
     * 
     * @throws IOException 如果页面跳转失败
     */
    private void handleCreateAccount() throws IOException {
        System.out.println("跳转注册页面");
        MainApp.showSignup();
    }

    /**
     * 显示错误信息。
     * 
     * @param message 错误信息
     */
    private void showError(String message) {
        Platform.runLater(() -> errorLabel.setText(message));
    }
}
