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
import org.softeng.group77.pennyplanner.util.CsvImporter;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
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
            String newId = UUID.randomUUID().toString(); // 使用UUID生成唯一ID
            double finalAmount = isExpense ? -Math.abs(amount) : Math.abs(amount);

            tableModel newTransaction = new tableModel(
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
        alert.setTitle("Success");
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
        fileChooser.setTitle("Choose Transaction Detail CSV File");

        // 设置文件过滤器，只接受CSV文件
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV文件", "*.csv")
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

                // 解析CSV文件并导入交易记录
                CsvImporter.ImportResult result = CsvImporter.importTransactions(destFile, MainApp.getTransactionAdapter());

                // 显示导入结果
                StringBuilder message = new StringBuilder();
                message.append("Import Successfully\n");
                message.append("Loaded RecordsL ").append(result.getTotalSuccessful()).append("\n");

                if (result.hasErrors()) {
                    message.append("\n出现以下错误:\n");
                    List<String> errors = result.getErrorMessages();
                    // 限制显示的错误数量，以防对话框过大
                    int displayLimit = Math.min(errors.size(), 5);
                    for (int i = 0; i < displayLimit; i++) {
                        message.append("• ").append(errors.get(i)).append("\n");
                    }
                    if (errors.size() > displayLimit) {
                        message.append("... 以及其他 ").append(errors.size() - displayLimit).append(" 个错误\n");
                    }
                    message.append("\nDetailed import log saved to: uploads/import_log_")
                            .append(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")))
                            .append(".txt");

                    // 保存详细错误日志
                    saveErrorLog(errors);
                }

                // 如果有成功导入的记录，刷新UI
                if (result.getTotalSuccessful() > 0) {
                    // 通知应用刷新数据
                    MainApp.refreshData();
                    message.append("\n\nData has been updated! You can check in History page.");
                }

                showAlert(result.hasErrors() ? Alert.AlertType.WARNING : Alert.AlertType.INFORMATION,
                        "导入结果", message.toString());

            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "导入失败", "处理CSV文件时出错: " + e.getMessage());
            }
        }
    }

    /**
     * 保存错误日志到文件
     */
    private void saveErrorLog(List<String> errors) {
        try {
            File logDir = new File("uploads");
            if (!logDir.exists()) logDir.mkdir();

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File logFile = new File(logDir, "import_log_" + timestamp + ".txt");

            try (PrintWriter writer = new PrintWriter(logFile)) {
                writer.println("CSV导入错误日志 - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                writer.println("-----------------------------");
                writer.println();

                for (String error : errors) {
                    writer.println(error);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示警告/信息对话框
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // 对于长消息，使用TextArea显示
        if (message.length() > 200) {
            TextArea textArea = new TextArea(message);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxHeight(250);

            alert.getDialogPane().setContent(textArea);
        }

        alert.showAndWait();
    }

    @FXML
    private void showImportHelp() {
        Alert helpDialog = new Alert(Alert.AlertType.INFORMATION);
        helpDialog.setTitle("CSV Import Help");
        helpDialog.setHeaderText("How to prepare Transaction Records CSV File");

        String helpContent = "CSV file should contain following columns\n\n" +
                "1. date - YYYY-MM-DD (e.g. 2024-05-15)\n" +
                "2. description - Description of each entry\n" +
                "3. amount - Expense is negative，income is positive\n" +
                "4. category - Must be one of the followings：\n" +
                "   • Food\n" +
                "   • Salary\n" +
                "   • Living Bill\n" +
                "   • Entertainment\n" +
                "   • Transportation\n" +
                "   • Education\n" +
                "   • Clothes\n" +
                "   • Others\n" +
                "5. method - Must be one of the followings：\n" +
                "   • Credit Card\n" +
                "   • Bank Transfer\n" +
                "   • Auto-Payment\n" +
                "   • Cash\n" +
                "   • E-Payment\n\n" +
                "Sample CSV：\n" +
                "date,description,amount,category,method\n" +
                "2024-05-01,Grocery Shopping,-50.75,Food,Cash\n" +
                "2024-05-03,Salary Deposit,3000.00,Salary,Bank Transfer\n\n" +
                "You can download sample CSV file for reference。";

        TextArea textArea = new TextArea(helpContent);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefHeight(300);
        textArea.setPrefWidth(500);

        helpDialog.getDialogPane().setContent(textArea);

        Button downloadButton = new Button("Download Sample CSV");
        downloadButton.setOnAction(e -> downloadExampleCsv());

        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.OK_DONE);
        helpDialog.getButtonTypes().setAll(closeButton);

        // 添加自定义按钮到对话框
        helpDialog.setGraphic(downloadButton);

        helpDialog.showAndWait();
    }

    /**
     * 下载示例CSV文件
     */
    private void downloadExampleCsv() {
        try {
            // 创建一个示例CSV内容
            String exampleCsvContent = "date,description,amount,category,method\n" +
                    "2025-06-01,Grocery Shopping,-50.75,Food,Cash\n" +
                    "2025-06-03,Salary Deposit,3000.00,Salary,Bank Transfer\n" +
                    "2025-06-05,Electricity Bill,-120.35,Living Bill,Auto-Payment\n" +
                    "2025-06-07,Movie Night,-25.50,Entertainment,Credit Card\n" +
                    "2025-06-10,Bus Pass,-45.00,Transportation,E-Payment\n";

            // 让用户选择保存位置
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Download Sample CSV");
            fileChooser.setInitialFileName("example_transactions.csv");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV文件", "*.csv"));

            File file = fileChooser.showSaveDialog(getCurrentStage());
            if (file != null) {
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(exampleCsvContent);
                }
                showAlert(Alert.AlertType.INFORMATION, "Downloaded Successfully", "Sample CSV format has been saved:\n" + file.getAbsolutePath());
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "下载失败", "无法保存示例CSV文件: " + e.getMessage());
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
























