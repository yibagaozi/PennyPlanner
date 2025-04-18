package org.softeng.group77.pennyplanner.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ManagementController {
    // 获取当前Stage的两种方式（任选其一）
    private Stage getCurrentStage() {
        // 方式1：通过任意界面元素获取（比如上传按钮）
        return (Stage) uploadButton.getScene().getWindow();

        // 方式2：通过MainApp的静态方法获取（如果存在）
        // return MainApp.getPrimaryStage();
    }

    @FXML
    private Button uploadButton; // 对应FXML中的上传按钮

    // 文件上传核心方法
    @FXML
    private void handleFileUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择上传文件");

        // 设置文件过滤器
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("所有文件", "*.*"),
                new FileChooser.ExtensionFilter("数据文件", "*.csv", "*.xlsx")
        );

        // 获取当前窗口
        Stage currentStage = getCurrentStage();

        // 显示文件选择对话框
        File selectedFile = fileChooser.showOpenDialog(currentStage);

        if (selectedFile != null) {
            try {
                // 创建目标目录
                File destDir = new File("uploads");
                if (!destDir.exists()) destDir.mkdir();

                // 构造目标路径
                File destFile = new File(destDir, selectedFile.getName());

                // 执行文件复制
                Files.copy(
                        selectedFile.toPath(),
                        destFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                );

                System.out.println("文件上传成功 ▶ " + destFile.getAbsolutePath());
            } catch (IOException e) {
                System.out.println("上传失败: " + e.getMessage());
            }
        }
    }

    @FXML
    private void turntoHome() throws IOException {
        System.out.println("转到home页面");
        MainApp.showHome();
    }
    @FXML
    private void turntoReport() throws IOException {
        System.out.println("转到home页面");
        //MainApp.showReport();
    }@FXML
    private void turntoHistory() throws IOException {
        System.out.println("转到home页面");
        MainApp.showhistory();
    }@FXML
    private void turntoManagement() throws IOException {
        System.out.println("转到home页面");
        MainApp.showmanagement();
    }@FXML
    private void turntoUser() throws IOException {
        System.out.println("转到home页面");
        MainApp.showuser();
    }
    @FXML
    private void turntoLogin() throws IOException {
        System.out.println("Login");
        MainApp.showLogin();
    }
    @FXML
    private void useAI() throws IOException {
        System.out.println("调用api接口");
    }
}
