package org.softeng.group77.pennyplanner.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.softeng.group77.pennyplanner.adapter.TransactionAdapter;
import org.softeng.group77.pennyplanner.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Controller class for handling transaction history view.
 * Manages display and filtering of transaction records in a TableView with
 * filtering capabilities by year, month and category.
 * @author CHAI Jiayang
 * @version 2.0.0
 * @since 1.0.0
 */
@Controller
public class HistoryController {
    @FXML private Label date;
    @FXML private ComboBox<Integer> Year;
    @FXML private ComboBox<String> Month;
    @FXML private ComboBox<String> category;
    @FXML private TableView<tableModel> transactionTable;
    @FXML private TableColumn<tableModel, String> transactionidColumn;
    @FXML private TableColumn<tableModel, String> dateColumn;
    @FXML private TableColumn<tableModel, String> descriptionColumn;
    @FXML private TableColumn<tableModel, Double> amountColumn;
    @FXML private TableColumn<tableModel, String> categoryColumn;
    @FXML private TableColumn<tableModel, String> methodColumn;

    // 数据存储结构：原始数据 + 动态过滤列表
    private final ObservableList<tableModel> transactionData = SharedDataModel.getTransactionData();
    private FilteredList<tableModel> filteredData = new FilteredList<>(transactionData);

    @FXML
    private SplitPane splitPane;

    private TransactionAdapter transactionAdapter;
    private AuthService authService; // 添加AuthService

    /**
     * Sets the TransactionAdapter for this controller.
     * This method is used by Spring to inject the TransactionAdapter dependency.
     * @param transactionAdapter the TransactionAdapter to set
     */
    @Autowired
    public void setTransactionAdapter(TransactionAdapter transactionAdapter) {
        this.transactionAdapter = transactionAdapter;
    }

    /**
     * Sets the AuthService for this controller.
     * This method is used by Spring to inject the AuthService dependency.
     * @param authService the AuthService to set
     */
    @Autowired
    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    @FXML
    private Button classifyButton;

    private final ApplicationContext applicationContext;

    /**
     * Constructor for HistoryController.
     * @param applicationContext Spring application context for dependency injection
     */
    public HistoryController(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Initializes the controller after FXML loading. Sets up:
     * - ComboBox options for year/month/category filters
     * - TableView column bindings and formatting
     * - Dynamic filtering logic
     * - Special cell renderers for ID and amount columns
     */
    @FXML
    private void initialize() {
        Year.setPromptText("Year");
        Month.setPromptText("Month");
        category.setPromptText("Category");

        // 确保在页面初始化时就刷新数据
        SharedDataModel.refreshTransactionData();

        Year.setItems(FXCollections.observableArrayList(
                null, // 空选项
                2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021,
                2022, 2023, 2024, 2025, 2026, 2027, 2028, 2029, 2030
        ));

        // 月份选择框 (1-12月)
        Month.setItems(FXCollections.observableArrayList(
                null, // 空选项
                "01", "02", "03", "04", "05", "06",
                "07", "08", "09", "10", "11", "12"
        ));


        // 初始化分类选择框
        ObservableList<String> categories = FXCollections.observableArrayList(
                null, "Food", "Salary", "Living Bill", "Entertainment", "Transportation", "Education", "Clothes", "Others"
        );
        category.setItems(categories);

        // 绑定表格列与模型属性
        transactionidColumn.setCellValueFactory(cellData -> cellData.getValue().displayIdProperty());
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        amountColumn.setCellValueFactory(cellData -> cellData.getValue().amountProperty().asObject());
        categoryColumn.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());
        methodColumn.setCellValueFactory(cellData -> cellData.getValue().methodProperty());
        // 设置默认按日期降序
        dateColumn.setSortType(TableColumn.SortType.DESCENDING);
        transactionTable.getSortOrder().add(dateColumn);

        // 设置动态过滤列表为表格数据源
        transactionTable.setItems(filteredData);

        // 加载示例数据（仅加载一次）
        if (transactionData.isEmpty()) {
            //addSampleData();
        }

        // 配置金额列显示格式
        amountColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("$%.2f", amount));
                    setStyle(amount < 0 ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
                }
            }
        });

        // 为日期列添加格式化
        dateColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(date);
                    setStyle("-fx-alignment: CENTER;"); // 居中对齐
                }
            }
        });

        // 为类别列添加格式化和emoji
        categoryColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String category, boolean empty) {
                super.updateItem(category, empty);
                if (empty || category == null) {
                    setText(null);
                    setStyle("");
                } else {
                    // 根据类别添加emoji
                    String emoji = getEmojiForCategory(category);
                    setText(emoji + " " + category);
                    setStyle("-fx-alignment: CENTER-LEFT; -fx-padding: 0 0 0 10px;");
                }
            }
        });

        // 为支付方式列添加格式化和emoji (这是新增的关键部分)
        methodColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String method, boolean empty) {
                super.updateItem(method, empty);
                if (empty || method == null) {
                    setText(null);
                    setStyle("");
                } else {
                    // 根据支付方式添加emoji
                    String emoji = getEmojiForMethod(method);
                    setText(emoji + " " + method);
                    setStyle("-fx-alignment: CENTER-LEFT; -fx-padding: 0 0 0 10px;");
                }
            }
        });

        // 绑定筛选条件监听器
        Year.valueProperty().addListener((obs, oldVal, newVal) -> updateFilter());
        Month.valueProperty().addListener((obs, oldVal, newVal) -> updateFilter());
        category.valueProperty().addListener((obs, oldVal, newVal) -> updateFilter());

        // Initial filter application might be needed if default filters are set
        updateFilter();

        // 为编号列设置一个特殊的cellFactory，动态生成序号
        transactionidColumn.setCellFactory(column -> new TableCell<tableModel, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    // 使用当前行索引+1作为编号
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });
        // 不再需要使用model中的displayId作为值
        transactionidColumn.setCellValueFactory(new PropertyValueFactory<>("id")); // 任意属性，实际不会使用


        // 禁用分割线的拖动
        splitPane.getDividers().forEach(divider -> divider.positionProperty().addListener((observable, oldValue, newValue) -> {
            divider.setPosition(0.12); // 固定分割线位置为 10%
        }));

        // 重新应用过滤条件并刷新表格
        updateFilter();
        transactionTable.refresh();

        // 分类按钮 -- 事件处理
        if (classifyButton != null) {
            classifyButton.setOnAction(e -> openClassificationWindow());
        }
    }

    /**
     * Updates the filter based on selected year, month, and category.
     * This method is called whenever the filter criteria change.
     */
    private void updateFilter() {
        Predicate<tableModel> predicate = transaction -> {
            // 日期条件处理
            boolean dateMatch = true;
            if (Year.getValue() != null || Month.getValue() != null) {
                String[] dateParts = transaction.getDate().split("-");
                int year = Integer.parseInt(dateParts[0]);
                int month = Integer.parseInt(dateParts[1]);

                // 处理未选择年份/月份的情况
                int selectedYear = Year.getValue() != null ? Year.getValue() : year;
                int selectedMonth = Month.getValue() != null ?
                        Integer.parseInt(Month.getValue().replace("月", "")) : month;

                dateMatch = (year == selectedYear) && (month == selectedMonth);
            }

            // 分类条件处理
            boolean categoryMatch = category.getValue() == null ||
                    category.getValue().isEmpty() ||
                    transaction.getCategory().equals(category.getValue());

            return dateMatch && categoryMatch;
        };

        filteredData.setPredicate(predicate);
    }

    /**
     * Refreshes the transaction data and re-applies the current filters.
     * This method can be called to update the displayed data after any changes.
     */
    @FXML
    public void refreshData() {
        SharedDataModel.refreshTransactionData();
        // 重新应用过滤条件
        updateFilter();
        // 刷新表格显示
        transactionTable.refresh();
    }

    /**
     * Styles the transaction ID column with emojis.
     * @param column the TableColumn to style
     */
    private void styleColumnWithEmoji(TableColumn<tableModel, String> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    // 根据类别或支付方式添加emoji
                    String emoji = getEmojiForCategory(item);
                    setText(emoji + " " + item);
                    setStyle("-fx-alignment: CENTER-LEFT; -fx-padding: 0 0 0 10px;");
                }
            }
        });
    }

    /**
     * Return the emoji for the transaction category.
     * @param category the transaction category
     */
    private String getEmojiForCategory(String category) {
        if (category == null) return "";

        switch (category) {
            case "Food": return "🍔";
            case "Salary": return "💰";
            case "Living Bill": return "🏠";
            case "Entertainment": return "🎬";
            case "Transportation": return "🚗";
            case "Education": return "🎓";
            case "Clothes": return "👕";
            default: return "🔖";
        }
    }

    /**
     * Return the emoji for the payment method.
     * @param method the payment method
     */
    private String getEmojiForMethod(String method) {
        if (method == null) return "❓";

        switch (method) {
            case "Credit Card": return "💳";
            case "Bank Transfer": return "🏦";
            case "Auto-Payment": return "\uD83E\uDD16";
            case "Cash": return "💵";
            case "E-Payment": return "📱";
            default: return "💲";
        }
    }

    /**
     * Handle the edit button event
     */
    @FXML
    public void handleEdit() {
        // 首先检查用户是否已登录
        try {
            if (authService.getCurrentUser() == null) {
                showAlert("未登录", "您需要登录才能编辑交易记录", Alert.AlertType.WARNING);
                return;
            }
        } catch (Exception e) {
            showAlert("认证错误", "无法验证用户状态: " + e.getMessage(), Alert.AlertType.ERROR);
            return;
        }

        tableModel selectedTransaction = transactionTable.getSelectionModel().getSelectedItem();
        if (selectedTransaction != null) {
            try {
                // 加载编辑对话框
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getClassLoader().getResource("fxml/Edit_Transaction_view.fxml"));
                loader.setControllerFactory(this.applicationContext::getBean);
                Parent root = loader.load();

                EditTransactionController controller = loader.getController();

                // 创建交易记录的深拷贝，避免直接修改表格中的对象
                tableModel transactionCopy = new tableModel(
                        selectedTransaction.getId(),
                        selectedTransaction.getDate(),
                        selectedTransaction.getDescription(),
                        selectedTransaction.getAmount(),
                        selectedTransaction.getCategory(),
                        selectedTransaction.getMethod()
                );

                controller.setTransaction(transactionCopy);

                Stage dialogStage = new Stage();
                dialogStage.setTitle("Edit Transaction Entry");
                dialogStage.initModality(Modality.WINDOW_MODAL);
                dialogStage.initOwner(transactionTable.getScene().getWindow());
                dialogStage.setScene(new Scene(root));

                // 显示对话框并等待用户关闭
                dialogStage.showAndWait();

                // 如果用户点击了"保存"按钮，则更新记录
                if (controller.isSaveClicked()) {
                    try {
                        // 将修改后的数据复制回原始对象
                        selectedTransaction.setDate(transactionCopy.getDate());
                        selectedTransaction.setDescription(transactionCopy.getDescription());
                        selectedTransaction.setAmount(transactionCopy.getAmount());
                        selectedTransaction.setCategory(transactionCopy.getCategory());
                        selectedTransaction.setMethod(transactionCopy.getMethod());

                        boolean success = SharedDataModel.updateTransaction(selectedTransaction);
                        if (success) {
                            showAlert("Success", "Updated Successfully", Alert.AlertType.INFORMATION);
                            refreshData(); // 刷新表格数据
                        } else {
                            showAlert("更新失败", "无法更新交易记录。可能是因为您没有权限修改该记录或者用户会话已过期。", Alert.AlertType.ERROR);
                            // 回滚UI显示
                            refreshData();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        showAlert("错误", "更新交易记录失败: " + ex.getMessage(), Alert.AlertType.ERROR);
                        // 回滚UI显示
                        refreshData();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("错误", "打开编辑对话框失败: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    /**
     * Handle the delete button event
     */
    @FXML
    public void handleDelete() {
        tableModel selectedTransaction = transactionTable.getSelectionModel().getSelectedItem();
        if (selectedTransaction != null) {
            // 确认删除
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Delete");
            confirmAlert.setHeaderText("Delete this entry?");
            confirmAlert.setContentText("Description " + selectedTransaction.getDescription() +
                    "\nAmount: " + String.format("$%.2f", selectedTransaction.getAmount()) +
                    "\nDate: " + selectedTransaction.getDate());

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // 用户确认删除
                boolean success = SharedDataModel.deleteTransaction(selectedTransaction.getId());
                if (success) {
                    showAlert("Success", "Deleted Successfully", Alert.AlertType.INFORMATION);
                    refreshData(); // 刷新表格数据
                } else {
                    showAlert("Error", "Failed to delete", Alert.AlertType.ERROR);
                }
            }
        }
    }

    /**
     * 显示提示对话框
     */
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void openClassificationWindow() {
        try {
            // 创建FXML加载器
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/classification_window_view.fxml"));
            loader.setControllerFactory(applicationContext::getBean);

            // 加载布局
            Scene scene = new Scene(loader.load());

            // 设置窗口
            Stage dialogStage = new Stage();
            dialogStage.setTitle("AI-Classification");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(scene);

            // 显示窗口并等待关闭
            dialogStage.showAndWait();

            // 获取控制器
            ClassificationWindowController controller = loader.getController();
            if (controller.isConfirmClicked()) {
                // 如果用户确认使用分类结果，可以在这里处理
                String category = controller.getClassificationResult();
                // 可以用于预填充新增交易的分类字段或其他用途
            }

        } catch (IOException e) {
            e.printStackTrace();
            // 显示错误对话框
        }
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
