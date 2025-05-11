package org.softeng.group77.pennyplanner.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.softeng.group77.pennyplanner.adapter.TransactionAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
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

    @Autowired
    public void setTransactionAdapter(TransactionAdapter transactionAdapter) {
        this.transactionAdapter = transactionAdapter;
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
