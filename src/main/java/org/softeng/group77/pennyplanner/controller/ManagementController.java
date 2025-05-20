package org.softeng.group77.pennyplanner.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.math.BigDecimal;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.UUID;

public class ManagementController {
    // 字段绑定
    @FXML private DatePicker dateField;
    @FXML private TextField descriptionField;
    @FXML private TextField amountField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> methodComboBox;
    @FXML private SplitPane splitPane;
    // 类型选择状态
    private boolean isExpense = true;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // 类别和对应的emoji
    private final String[][] CATEGORIES = {
            {null, ""},
            {"Food", "🍔"},
            {"Salary", "💰"},
            {"Living Bill", "🏠"},
            {"Entertainment", "🎬"},
            {"Transportation", "🚗"},
            {"Education", "🎓"},
            {"Clothes", "👕"},
            {"Others", "🔖"}
    };

    // 支付方式和对应的emoji
    private final String[][] PAYMENT_METHODS = {
            {null, ""},
            {"Credit Card", "💳"},
            {"Bank Transfer", "🏦"},
            {"Auto-Payment", "\uD83E\uDD16"},
            {"Cash", "💵"},
            {"E-Payment", "📱"}
    };


        // 在initialize方法中初始化分类和支付方式
        public void initialize() {
            Locale.setDefault(Locale.ENGLISH);
//            // 初始化分类选项
//            categoryComboBox.setItems(FXCollections.observableArrayList(
//                    null, "Food 🍔", "Salary 💰", "Living Bill", "Entertainment",
//                    "Transportation", "Education", "Clothes", "Others"
//            ));
//
//            // 初始化支付方式
//            methodComboBox.setItems(FXCollections.observableArrayList(
//                    null, "Credit Card", "Bank Transfer", "Auto-Payment", "Cash", "E-Payment"
//            ));
//
//            // 设置默认选择
//            categoryComboBox.getSelectionModel().selectFirst();
//            methodComboBox.getSelectionModel().selectFirst();

            // 配置DatePicker
            dateField.setPromptText("Select Date");
            // 设置当前日期为默认日期
            dateField.setValue(LocalDate.now());
            // 设置日期格式
            dateField.setConverter(new javafx.util.StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    if (date != null) {
                        return DATE_FORMATTER.format(date);
                    } else {
                        return "";
                    }
                }

                @Override
                public LocalDate fromString(String string) {
                    if (string != null && !string.isEmpty()) {
                        return LocalDate.parse(string, DATE_FORMATTER);
                    } else {
                        return null;
                    }
                }
            });

            // 初始化分类选择器
            setupCategoryComboBox();
            // 初始化支付方式选择器
            setupMethodComboBox();

            // 禁用分割线的拖动
            splitPane.getDividers().forEach(divider -> divider.positionProperty().addListener((observable, oldValue, newValue) -> {
                divider.setPosition(0.1); // 固定分割线位置为 10%
            }));
        }


    private void setupCategoryComboBox() {
        // 将二维数组的第一列（类别名称）提取为一维数组
        String[] categoryNames = new String[CATEGORIES.length];
        for (int i = 0; i < CATEGORIES.length; i++) {
            categoryNames[i] = CATEGORIES[i][0];
        }

        categoryComboBox.setItems(FXCollections.observableArrayList(categoryNames));

        // 设置单元格工厂
        categoryComboBox.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String category, boolean empty) {
                super.updateItem(category, empty);

                if (empty || category == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // 查找对应的emoji
                    String emoji = "";
                    for (String[] cat : CATEGORIES) {
                        if (category.equals(cat[0])) {
                            emoji = cat[1];
                            break;
                        }
                    }

                    // 创建带有emoji的显示项
                    HBox hbox = new HBox(10); // 10是间距
                    hbox.setAlignment(Pos.CENTER_LEFT);

                    Text emojiText = new Text(emoji);
                    emojiText.setFont(Font.font(14)); // emoji稍大一点

                    Text categoryText = new Text(category);

                    hbox.getChildren().addAll(emojiText, categoryText);
                    setGraphic(hbox);
                    setText(null); // 因为我们使用自定义节点，所以setText设为null
                }
            }
        });

        // 设置按钮单元格
        categoryComboBox.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String category, boolean empty) {
                super.updateItem(category, empty);

                if (empty || category == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // 查找对应的emoji
                    String emoji = "";
                    for (String[] cat : CATEGORIES) {
                        if (category.equals(cat[0])) {
                            emoji = cat[1];
                            break;
                        }
                    }

                    // 直接在按钮单元格中显示emoji + 类别名称
                    setText(emoji + " " + category);
                }
            }
        });

        categoryComboBox.getSelectionModel().selectFirst();
    }

    private void setupMethodComboBox() {
        // 将二维数组的第一列（支付方式名称）提取为一维数组
        String[] methodNames = new String[PAYMENT_METHODS.length];
        for (int i = 0; i < PAYMENT_METHODS.length; i++) {
            methodNames[i] = PAYMENT_METHODS[i][0];
        }

        methodComboBox.setItems(FXCollections.observableArrayList(methodNames));

        // 设置单元格工厂
        methodComboBox.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String method, boolean empty) {
                super.updateItem(method, empty);

                if (empty || method == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // 查找对应的emoji
                    String emoji = "";
                    for (String[] m : PAYMENT_METHODS) {
                        if (method.equals(m[0])) {
                            emoji = m[1];
                            break;
                        }
                    }

                    // 创建带有emoji的显示项
                    HBox hbox = new HBox(10);
                    hbox.setAlignment(Pos.CENTER_LEFT);

                    Text emojiText = new Text(emoji);
                    emojiText.setFont(Font.font(14));

                    Text methodText = new Text(method);

                    hbox.getChildren().addAll(emojiText, methodText);
                    setGraphic(hbox);
                    setText(null);
                }
            }
        });

        // 设置按钮单元格
        methodComboBox.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String method, boolean empty) {
                super.updateItem(method, empty);

                if (empty || method == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // 查找对应的emoji
                    String emoji = "";
                    for (String[] m : PAYMENT_METHODS) {
                        if (method.equals(m[0])) {
                            emoji = m[1];
                            break;
                        }
                    }

                    // 直接在按钮单元格中显示emoji + 方法名称
                    setText(emoji + " " + method);
                }
            }
        });

        methodComboBox.getSelectionModel().selectFirst();
    }



        // "Save"按钮处理方法
        @FXML
        private void handleSave() {
            try {
                //dateField.setPromptText("YYYY-MM-DD");
                // 数据校验
//                if (dateField.getText().isEmpty() ||
//                        !dateField.getText().matches("\\d{4}-\\d{2}-\\d{2}")) {
//                    showAlert("日期格式错误，请使用YYYY-MM-DD格式");
//                    return;
//                }

                // 检查日期是否已选择
                if (dateField.getValue() == null) {
                    showAlert("请选择日期");
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
                        //dateField.getText(),
                        dateField.getValue().format(DATE_FORMATTER), // 从DatePicker获取格式化日期
                        descriptionField.getText(),
                        finalAmount,
                        categoryComboBox.getValue(),
                        methodComboBox.getValue()
                );

                //添加到共享数据
                SharedDataModel.getTransactionData().add(newTransaction);
                // 添加到共享数据并持久化
                boolean success = SharedDataModel.addTransaction(newTransaction);

                if (success) {
                    showSuccessAlert("Saved Successfully");
                    // 清空输入框
                    clearForm();
                } else {
                    showAlert("Failed. Try again later.");
                }

                // 清空输入框
                clearForm();

            } catch (NumberFormatException e) {
                showAlert("Invalid Amount");
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
            // 重置DatePicker为当前日期
            dateField.setValue(LocalDate.now());
            //dateField.clear();
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

        private void showSuccessAlert(String message) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("操作成功");
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
                    showSuccessAlert("文件上传成功: " + destFile.getAbsolutePath());
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
























