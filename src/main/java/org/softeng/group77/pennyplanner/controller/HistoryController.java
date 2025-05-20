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
import org.softeng.group77.pennyplanner.adapter.TransactionAdapter;
import org.softeng.group77.pennyplanner.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Predicate;

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

    @Autowired
    public void setTransactionAdapter(TransactionAdapter transactionAdapter) {
        this.transactionAdapter = transactionAdapter;
    }

    @Autowired
    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

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
            divider.setPosition(0.1); // 固定分割线位置为 10%
        }));

        // 重新应用过滤条件并刷新表格
        updateFilter();
        transactionTable.refresh();
    }

    // 统一筛选逻辑（核心修复）
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


    @FXML
    public void refreshData() {
        SharedDataModel.refreshTransactionData();
        // 重新应用过滤条件
        updateFilter();
        // 刷新表格显示
        transactionTable.refresh();
    }

    // 为类别和支付方式列添加emoji和样式
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

    // 根据类别返回对应的emoji
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

    // 根据支付方式返回对应的emoji
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
     * 处理编辑按钮点击事件
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
                dialogStage.setTitle("编辑交易记录");
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
                            showAlert("成功", "交易记录已成功更新", Alert.AlertType.INFORMATION);
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
     * 处理删除按钮点击事件
     */
    @FXML
    public void handleDelete() {
        tableModel selectedTransaction = transactionTable.getSelectionModel().getSelectedItem();
        if (selectedTransaction != null) {
            // 确认删除
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("确认删除");
            confirmAlert.setHeaderText("您确定要删除此交易记录吗？");
            confirmAlert.setContentText("描述: " + selectedTransaction.getDescription() +
                    "\n金额: " + String.format("$%.2f", selectedTransaction.getAmount()) +
                    "\n日期: " + selectedTransaction.getDate());

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // 用户确认删除
                boolean success = SharedDataModel.deleteTransaction(selectedTransaction.getId());
                if (success) {
                    showAlert("成功", "交易记录已成功删除", Alert.AlertType.INFORMATION);
                    refreshData(); // 刷新表格数据
                } else {
                    showAlert("错误", "删除交易记录失败", Alert.AlertType.ERROR);
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


    // 以下导航方法保持不变
    @FXML
    private void turntoHome() throws IOException {
        MainApp.showHome();
    }
    @FXML
    private void turntoReport() throws IOException {
        MainApp.showReport();
    }@FXML
    private void turntoHistory() throws IOException {
        MainApp.showhistory();
    }@FXML
    private void turntoManagement() throws IOException {
        MainApp.showmanagement();
    }@FXML
    private void turntoUser() throws IOException {
        MainApp.showuser();
    }
    @FXML
    private void turntoLogin() throws IOException {
        System.out.println("Login");
        MainApp.showLogin();
    }
}
