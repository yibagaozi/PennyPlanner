package org.softeng.group77.pennyplanner.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.util.function.Predicate;

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
    //private final ObservableList<tableModel> transactionData = FXCollections.observableArrayList();
    private final ObservableList<tableModel> transactionData = SharedDataModel.getTransactionData();
    private FilteredList<tableModel> filteredData = new FilteredList<>(transactionData);

    @FXML
    private SplitPane splitPane;

    @FXML
    private void initialize() {
        // 首先刷新数据，确保显示最新的交易记录
        //SharedDataModel.refreshTransactionData();

        // 创建FilteredList包装SharedDataModel的数据
        //filteredData = new FilteredList<>(SharedDataModel.getTransactionData());

        // 初始化日期标题（固定显示 March 2025）
        //date.setText("April 2025");

        Year.setItems(FXCollections.observableArrayList(
                null, // 空选项
                2022, 2023, 2024, 2025
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
//        // 不再需要从模型中获取displayId
//        transactionidColumn.setCellValueFactory(cellData ->
//                new SimpleStringProperty(String.valueOf(filteredData.indexOf(cellData.getValue()) + 1)));


        // 禁用分割线的拖动
        splitPane.getDividers().forEach(divider -> divider.positionProperty().addListener((observable, oldValue, newValue) -> {
            divider.setPosition(0.1); // 固定分割线位置为 10%
        }));
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

    // 示例数据初始化（与截图完全一致）
//    private void addSampleData() {
//        transactionData.add(new tableModel("1", "2023-06-15", "超市购物", -125.50, "Food", "信用卡"));
//        transactionData.add(new tableModel("2", "2023-06-16", "工资收入", 5000.00, "Salary", "银行转账"));
//        transactionData.add(new tableModel("3", "2023-06-17", "水电费", -230.75, "Living Bill", "自动扣款"));
//    }

    @FXML
    public void refreshData() {
        SharedDataModel.refreshTransactionData();
        // 重新初始化过滤数据
        //filteredData = new FilteredList<>(SharedDataModel.getTransactionData());
        //transactionTable.setItems(filteredData);
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
