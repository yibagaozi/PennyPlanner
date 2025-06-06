package org.softeng.group77.pennyplanner.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.softeng.group77.pennyplanner.service.TransactionAnalysisService;
import org.softeng.group77.pennyplanner.util.CsvImporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

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

/**
 * Controller for the transaction management view in the PennyPlanner application.
 * This controller handles the creation, editing, and importing of financial transactions.
 * The controller integrates with the SharedDataModel for persistent storage
 * and the TransactionAnalysisService for AI-powered transaction classification.
 *
 * @author CHAI Jiayang
 * @author WANG Bingsong
 * @version 2.0.0
 * @since 1.1.0
 */
@Controller
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

    @FXML
    private Button classifyButton;

    @FXML private Label classificationStatusLabel; // 显示分类状态
    @FXML private ProgressIndicator classifyProgress; // 显示处理中状态

    private ApplicationContext applicationContext;

    /**
     * Sets the Spring application context for bean access
     *
     * @param applicationContext the Spring application context
     */
    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Initializes the controller after FXML elements are loaded.
     * Sets up date picker, category and payment method combo boxes,
     * and configures layout constraints.
     */
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
            divider.setPosition(0.12); // 固定分割线位置为 10%
        }));

        if (classifyButton != null) {
            classifyButton.setOnAction(e -> openClassificationWindow());
        }

        // 初始化AI分类组件
        if (classificationStatusLabel != null) {
            classificationStatusLabel.setText("Ready to classify");
        }

        if (classifyProgress != null) {
            classifyProgress.setVisible(false);
        }

        if (classifyButton != null) {
            classifyButton.setOnAction(e -> openClassificationWindow());
        }
    }

    /**
     * Sets up the category combo box with emoji icons
     */
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

    /**
     * Sets up the payment method combo box with emoji icons
     */
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

    /**
     * Handles the save button click event.
     * Validates input data, creates a transaction record,
     * and saves it to the shared data model.
     */
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

    /**
     * Handles the cancel button click event.
     * Clears all input fields.
     */
    @FXML
    private void handleCancel() {
        // 清空输入框
        clearForm();
    }

    /**
     * Sets the transaction type to expense
     */
    @FXML
    private void handleExpense() {
        isExpense = true;
    }

    /**
     * Sets the transaction type to income
     */
    @FXML
    private void handleIncome() {
        isExpense = false;
    }

    /**
     * Clears all input fields and resets to default values
     */
    private void clearForm() {
        // 重置DatePicker为当前日期
        dateField.setValue(LocalDate.now());
        //dateField.clear();
        descriptionField.clear();
        amountField.clear();
        categoryComboBox.getSelectionModel().selectFirst();
        methodComboBox.getSelectionModel().selectFirst();
    }

    /**
     * Displays an error alert dialog
     *
     * @param message the error message to display
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("输入错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays a success alert dialog
     *
     * @param message the success message to display
     */
    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Gets the current window (Stage)
     *
     * @return the current Stage
     */
    private Stage getCurrentStage() {
        // 方式1：通过任意界面元素获取（比如上传按钮）
        return (Stage) uploadButton.getScene().getWindow();
        // 方式2：通过MainApp的静态方法获取（如果存在）
        // return MainApp.getPrimaryStage();
    }

    @FXML
    private Button uploadButton; // 对应FXML中的上传按钮

    /**
     * Handles file upload button click.
     * Opens a file chooser for selecting CSV files,
     * then processes and imports the selected file.
     */
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
     * Saves error log to a file
     *
     * @param errors the list of error messages to save
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

    /**
     * Shows help information for CSV import
     */
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
     * Downloads an example CSV file
     */
    private void downloadExampleCsv() {
        try {
            // 创建一个示例CSV内容
            String exampleCsvContent = "date,description,amount,category,method\n" +
                    "2025-05-01,Grocery Shopping,-50.75,Food,Cash\n" +
                    "2025-05-07,Salary Deposit,3000.00,Salary,Bank Transfer\n" +
                    "2025-05-13,Electricity Bill,-120.35,Living Bill,Auto-Payment\n" +
                    "2025-05-19,Movie Night,-25.50,Entertainment,Credit Card\n" +
                    "2025-05-25,Bus Pass,-45.00,Transportation,E-Payment\n"+
                    "2025-06-05,Taxi,-43.00,Transportation,E-Payment\n"+
                    "2025-06-10,Grocery,-100.00,Food,Cash\n" +
                    "2025-06-13,ScholarShip,5000.00,Education,Bank Transfer\n" +
                    "2025-06-13,Electricity Bill,-140.00,Living Bill,Auto-Payment\n" +
                    "2025-06-19,Party,-300.00,Entertainment,Credit Card\n" +
                    "2025-06-25,Bus Pass,-45.00,Transportation,E-Payment\n";

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

    /**
     * Opens the AI classification window to analyze transaction descriptions
     * and recommend appropriate categories
     */
    @FXML
    private void openClassificationWindow() {
        // 获取当前描述字段内容
        String description = descriptionField.getText();

        if (description == null || description.trim().isEmpty()) {
            classificationStatusLabel.setText("Type description first");
            classificationStatusLabel.setTextFill(Color.RED);
            return;
        }

        // 显示处理中状态
        classifyButton.setDisable(true);
        classifyProgress.setVisible(true);
        classificationStatusLabel.setText("Analysing...");
        classificationStatusLabel.setTextFill(Color.BLUE);

        // 获取TransactionAnalysisService
        TransactionAnalysisService transactionAnalysisService =
                applicationContext.getBean(TransactionAnalysisService.class);

        // 调用AI分类服务
        transactionAnalysisService.classifyTransaction(description)
                .thenAccept(category -> {
                    Platform.runLater(() -> {
                        classificationStatusLabel.setText("Recommended: " + category);
                        classificationStatusLabel.setTextFill(Color.GREEN);
                        categoryComboBox.setValue(category);
                        classifyButton.setDisable(false);
                        classifyProgress.setVisible(false);
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        classificationStatusLabel.setText("Failed to classify " + ex.getMessage());
                        classificationStatusLabel.setTextFill(Color.RED);
                        classifyButton.setDisable(false);
                        classifyProgress.setVisible(false);
                    });
                    return null;
                });
    }

    /**
     * Navigates to the home view
     *
     * @throws IOException if navigation fails
     */
    @FXML
    private void turntoHome() throws IOException {
        System.out.println("转到home页面");
        MainApp.showHome();
    }

    /**
     * Navigates to the report view
     *
     * @throws IOException if navigation fails
     */
    @FXML
    private void turntoReport() throws IOException {
        //System.out.println("转到home页面");
        MainApp.showReport();
    }

    /**
     * Navigates to the history view
     *
     * @throws IOException if navigation fails
     */
    @FXML
    private void turntoHistory() throws IOException {
        //System.out.println("转到home页面");
        MainApp.showhistory();
    }

    /**
     * Navigates to the management view
     *
     * @throws IOException if navigation fails
     */
    @FXML
    private void turntoManagement() throws IOException {
        //System.out.println("转到home页面");
        MainApp.showmanagement();
    }

    /**
     * Navigates to the user profile view
     *
     * @throws IOException if navigation fails
     */
    @FXML
    private void turntoUser() throws IOException {
        //System.out.println("转到home页面");
        MainApp.showuser();
    }

    /**
     * Navigates to the login view
     *
     * @throws IOException if navigation fails
     */
    @FXML
    private void turntoLogin() throws IOException {
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

    /**
     * Placeholder for AI functionality
     *
     * @throws IOException if operation fails
     */
    @FXML
    private void useAI() throws IOException {
        System.out.println("调用api接口");
    }

}
























