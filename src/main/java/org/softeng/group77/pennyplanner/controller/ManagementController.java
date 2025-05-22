package org.softeng.group77.pennyplanner.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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

/**
 * ManagementController 负责管理交易记录的增删改查，包括交易记录的保存、取消、文件上传及其他相关功能。
 * 该控制器提供界面上的用户输入、数据校验及与 SharedDataModel 的交互。
 */
public class ManagementController {

    // 字段绑定
    @FXML private TextField dateField;
    @FXML private TextField descriptionField;
    @FXML private TextField amountField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> methodComboBox;
    @FXML private SplitPane splitPane;
    
    // 类型选择状态
    private boolean isExpense = true;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 初始化方法，用于初始化类别和支付方式选择框，并设置默认选项。
     * 同时禁用分割线的拖动。
     */
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

        // 禁用分割线的拖动
        splitPane.getDividers().forEach(divider -> divider.positionProperty().addListener((observable, oldValue, newValue) -> {
            divider.setPosition(0.1); // 固定分割线位置为 10%
        }));
    }

    /**
     * 处理保存按钮点击事件，进行输入数据校验、交易记录创建并保存到共享数据中。
     */
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
            String newId = UUID.randomUUID().toString(); // 使用UUID生成唯一ID
            double finalAmount = isExpense ? -Math.abs(amount) : Math.abs(amount);

            tableModel newTransaction = new tableModel(
                    newId,
                    dateField.getText(),
                    descriptionField.getText(),
                    finalAmount,
                    categoryComboBox.getValue(),
                    methodComboBox.getValue()
            );

            // 添加到共享数据
            SharedDataModel.getTransactionData().add(newTransaction);

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

    /**
     * 处理取消按钮点击事件，清空所有输入框。
     */
    @FXML
    private void handleCancel() {
        // 清空输入框
        clearForm();
    }

    /**
     * 设置交易类型为支出。
     */
    @FXML
    private void handleExpense() {
        isExpense = true;
    }

    /**
     * 设置交易类型为收入。
     */
    @FXML
    private void handleIncome() {
        isExpense = false;
    }

    /**
     * 清空所有输入框和选择框。
     */
    private void clearForm() {
        dateField.clear();
        descriptionField.clear();
        amountField.clear();
        categoryComboBox.getSelectionModel().selectFirst();
        methodComboBox.getSelectionModel().selectFirst();
    }

    /**
     * 显示错误信息弹窗。
     * 
     * @param message 错误消息
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("输入错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * 获取当前窗口（Stage）。
     * 
     * @return 当前窗口的Stage
     */
    private Stage getCurrentStage() {
        // 方式1：通过任意界面元素获取（比如上传按钮）
        return (Stage) uploadButton.getScene().getWindow();
    }

    @FXML private Button uploadButton; // 对应FXML中的上传按钮

    /**
     * 处理文件上传事件，选择文件并将其复制到目标目录。
     */
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

    /**
     * 转到主页页面。
     * 
     * @throws IOException 如果加载页面失败
     */
    @FXML
    private void turntoHome() throws IOException {
        MainApp.showHome();
    }

    /**
     * 转到报告页面。
     * 
     * @throws IOException 如果加载页面失败
     */
    @FXML
    private void turntoReport() throws IOException {
        MainApp.showReport();
    }

    /**
     * 转到历史页面。
     * 
     * @throws IOException 如果加载页面失败
     */
    @FXML
    private void turntoHistory() throws IOException {
        MainApp.showhistory();
    }

    /**
     * 转到管理页面。
     * 
     * @throws IOException 如果加载页面失败
     */
    @FXML
    private void turntoManagement() throws IOException {
        MainApp.showmanagement();
    }

    /**
     * 转到用户页面。
     * 
     * @throws IOException 如果加载页面失败
     */
    @FXML
    private void turntoUser() throws IOException {
        MainApp.showuser();
    }

    /**
     * 转到登录页面。
     * 
     * @throws IOException 如果加载页面失败
     */
    @FXML
    private void turntoLogin() throws IOException {
        MainApp.showLogin();
    }

    /**
     * 调用 API 接口的方法（未实现）。
     * 
     * @throws IOException 如果调用 API 失败
     */
    @FXML
    private void useAI() throws IOException {
        System.out.println("调用api接口");
    }
}
