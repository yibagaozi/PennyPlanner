package org.softeng.group77.pennyplanner.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.math.BigDecimal;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;

public class ManagementController {
    // 字段绑定
    @FXML private TextField dateField;
    @FXML private TextField descriptionField;
    @FXML private TextField amountField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> methodComboBox;
    // 类型选择状态
    private boolean isExpense = true;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 在initialize方法中初始化分类和支付方式
        public void initialize() {
            // 初始化分类选项
            categoryComboBox.setItems(FXCollections.observableArrayList(
                    null, "Food", "Salary", "Living Bill", "Entertainment",
                    "Transportation", "Education", "Clothes", "Others"
            ));

            // 初始化支付方式
            methodComboBox.setItems(FXCollections.observableArrayList(
                    null, "Credit Card", "Bank Transfer", "Auto-Payment", "Cash", "E-Payment"
            ));

            // 设置默认选择
            categoryComboBox.getSelectionModel().selectFirst();
            methodComboBox.getSelectionModel().selectFirst();
        }

        // "Save"按钮处理方法
        @FXML
        private void handleSave() {
            try {
                dateField.setPromptText("YYYY-MM-DD");
                // 数据校验
                if (dateField.getText().isEmpty() ||
                        !dateField.getText().matches("\\d{4}-\\d{2}-\\d{2}")) {
                    showAlert("日期格式错误，请使用YYYY-MM-DD格式");
                    return;
                }

                double amount = Double.parseDouble(amountField.getText());
                if (amount <= 0) {
                    showAlert("金额必须大于0");
                    return;
                }

                // 创建新交易记录
                //String newId = String.valueOf(SharedDataModel.getTransactionData().size() + 1);
                String newId = UUID.randomUUID().toString(); // 使用UUID生成唯一ID
                double finalAmount = isExpense ? -Math.abs(amount) : Math.abs(amount);

                tableModel newTransaction = new tableModel(
                        //newId,
                        java.util.UUID.randomUUID().toString(), // 使用UUID作为后端ID
                        dateField.getText(),
                        descriptionField.getText(),
                        finalAmount,
                        categoryComboBox.getValue(),
                        methodComboBox.getValue()
                );

                // 添加到共享数据
                //SharedDataModel.getTransactionData().add(newTransaction);
                // 添加到共享数据并持久化
                boolean success = SharedDataModel.addTransaction(newTransaction);

                if (success) {
                    showAlert("交易记录已成功保存");
                    // 清空输入框
                    clearForm();
                } else {
                    showAlert("保存失败，请稍后再试");
                }

                // 清空输入框
                clearForm();

            } catch (NumberFormatException e) {
                showAlert("金额必须为有效数字");
            }
        }

        @FXML
        private void handleCancel() {
            // 清空输入框
            clearForm();
        }

        // 新增类型选择处理方法
        @FXML
        private void handleExpense() {
            isExpense = true;
        }

        @FXML
        private void handleIncome() {
            isExpense = false;
        }


        private void clearForm() {
            dateField.clear();
            descriptionField.clear();
            amountField.clear();
            categoryComboBox.getSelectionModel().selectFirst();
            methodComboBox.getSelectionModel().selectFirst();
        }

        private void showAlert(String message) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("输入错误");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }

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
            //System.out.println("转到home页面");
            MainApp.showReport();
        }@FXML
        private void turntoHistory() throws IOException {
            //System.out.println("转到home页面");
            MainApp.showhistory();
        }@FXML
        private void turntoManagement() throws IOException {
            //System.out.println("转到home页面");
            MainApp.showmanagement();
        }@FXML
        private void turntoUser() throws IOException {
            //System.out.println("转到home页面");
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
























