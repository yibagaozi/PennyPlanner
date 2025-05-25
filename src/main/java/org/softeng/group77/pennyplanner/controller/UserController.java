package org.softeng.group77.pennyplanner.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.softeng.group77.pennyplanner.dto.UserInfo;
import org.softeng.group77.pennyplanner.service.AuthService;
import org.softeng.group77.pennyplanner.util.JsonFileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.naming.AuthenticationException;
import java.io.File;

import java.io.IOException;

/**
 * Controller for the user profile management screen in the PennyPlanner application.
 * The controller integrates with the AuthService to authenticate password changes
 * and persist user information updates.
 *
 * @author CHAI Jiayang
 * @author WANG Bingsong
 * @version 2.0.0
 * @since 1.0.0
 */
@Controller
@Slf4j
public class UserController {
    @FXML
    SplitPane splitPane;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private PasswordField oldPasswordField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private ImageView avatarImageView;
    @FXML
    private Button uploadAvatarButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    @Autowired
    private AuthService authService;

    private final String avatarFolderPath = "data/avatars/";
    private String currentAvatarPath;

    /**
     * Initializes the controller after FXML elements are loaded.
     * Sets up layout constraints and loads the current user's data.
     */
    @FXML
    private void initialize() {
        // 禁用分割线的拖动
        splitPane.getDividers().forEach(divider -> divider.positionProperty().addListener((observable, oldValue, newValue) -> {
            divider.setPosition(0.12); // 固定分割线位置为 10%
        }));
        loadUserData();
        System.out.println("AuthService in UserController: " + authService);
        if (authService == null) {
            System.err.println("AuthService 未注入");}
    }

    /**
     * Loads the current user's profile data from the authentication service
     * and populates the form fields with this information.
     */
    private void loadUserData() {
        try {
            UserInfo currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                usernameField.setText(currentUser.getUsername());
                emailField.setText(currentUser.getEmail());
                phoneField.setText(currentUser.getPhone());

                // 加载头像
                currentAvatarPath = avatarFolderPath + currentUser.getId() + ".png";
                File avatarFile = new File(currentAvatarPath);
                if (avatarFile.exists()) {
                    avatarImageView.setImage(new Image(avatarFile.toURI().toString()));
                }
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load user information.");
        }
    }

    /**
     * Handles the avatar upload button click.
     */
    @FXML
    private void handleUploadAvatar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(uploadAvatarButton.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // 保存头像到指定文件夹
                File avatarDir = new File(avatarFolderPath);
                if (!avatarDir.exists()) {
                    avatarDir.mkdirs();
                }

                // 获取当前用户ID
                UserInfo currentUser = authService.getCurrentUser();
                if (currentUser == null) {
                    showAlert(Alert.AlertType.ERROR, "错误", "未找到当前用户信息");
                    return;
                }

                // 目标文件路径
                currentAvatarPath = avatarFolderPath + currentUser.getId() + ".png";
                File avatarFile = new File(currentAvatarPath);

                // 如果目标文件已存在则先删除
                if (avatarFile.exists()) {
                    avatarFile.delete();
                }

                // 使用Files.copy替代renameTo
                java.nio.file.Files.copy(
                        selectedFile.toPath(),
                        avatarFile.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );
                // Update avatar display - it's good to clear the cache for the image if using URLs
                // To ensure JavaFX reloads the image from the file system and not its cache:
                Image newImage = new Image(avatarFile.toURI().toString(), false); // false means don't use cache
                showAlert(Alert.AlertType.INFORMATION, "Success", "Avatar uploaded successfully.");
                // 更新头像显示
                avatarImageView.setImage(newImage);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to upload avatar.");
            }
        }
    }

    /**
     * Handles the save button click.
     */
    @FXML
    private void handleSave() {
        try {
            UserInfo currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "No user is logged in.");
                return;
            }

            // 更新用户资料
            currentUser.setUsername(usernameField.getText().trim());
            currentUser.setEmail(emailField.getText().trim());
            currentUser.setPhone(phoneField.getText().trim());
            authService.updateUserInfo(currentUser.getId(), currentUser);

            // 修改密码（如果填写了旧密码和新密码）
            String oldPassword = oldPasswordField.getText().trim();
            String newPassword = newPasswordField.getText().trim();
            if (!oldPassword.isEmpty() && !newPassword.isEmpty()) {
                authService.changePassword(currentUser.getId(), oldPassword, newPassword);
            }

            // 同步数据到 user.json
            //JsonFileUtil.updateJson("data/user.json", UserInfo.class, userInfo -> currentUser);

            // 提示保存成功
            showAlert(Alert.AlertType.INFORMATION, "Success", "User information updated successfully.");
        } catch (AuthenticationException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Password update failed: " + e.getMessage());
        }
        catch (IOException e) {
            log.error("Error saving user information: ", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save user information.");
            e.printStackTrace();
        }
        catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred.");
            e.printStackTrace();
        }
    }

    /**
     * Handles the cancel button click.
     */
    @FXML
    private void handleCancel() {
        // 重新加载用户数据
        loadUserData();
        oldPasswordField.clear();
        newPasswordField.clear();
    }

    /**
     * Displays an alert dialog with the specified type, title, and message
     *
     * @param alertType the type of alert to display
     * @param title the title of the alert
     * @param message the message content
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Navigates to the home view
     *
     * @throws IOException if navigation fails
     */
    @FXML
    private void turntoHome() throws IOException {
        MainApp.showHome();
    }

    /**
     * Navigates to the report view
     *
     * @throws IOException if navigation fails
     */
    @FXML
    private void turntoReport() throws IOException {
        MainApp.showReport();
    }

    /**
     * Navigates to the history view
     *
     * @throws IOException if navigation fails
     */
    @FXML
    private void turntoHistory() throws IOException {
        MainApp.showhistory();
    }

    /**
     * Navigates to the management view
     *
     * @throws IOException if navigation fails
     */
    @FXML
    private void turntoManagement() throws IOException {
        MainApp.showmanagement();
    }

    /**
     * Navigates to the user profile view
     *
     * @throws IOException if navigation fails
     */
    @FXML
    private void turntoUser() throws IOException {
        MainApp.showuser();
    }

    /**
     * Navigates to the login view
     *
     * @throws IOException if navigation fails
     */
    @FXML
    private void turntoLogin() throws IOException {
        System.out.println("Login");
        MainApp.showLogin();
    }

    /**
     * Navigates to the financial assistant view
     *
     * @throws IOException if navigation fails
     */
    @FXML
    private void turntoFinancialAssistant() throws IOException {
        MainApp.showFinancialAssistant();
    }
}
