package org.softeng.group77.pennyplanner.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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

//        //时间排序
//        dateColumn.setSortType(TableColumn.SortType.DESCENDING); // 默认按日期降序
//        transactionTable.getSortOrder().add(dateColumn); // 设置默认排序列
//        transactionTable.sort(); // 应用排序
//
//        // 确保表格可排序
//        transactionTable.setSortPolicy(table -> {
//            FXCollections.sort(transactionTable.getItems(), (o1, o2) -> {
//                // 如果用户点击了列头，使用默认排序
//                if (!transactionTable.getSortOrder().isEmpty()) {
//                    return table.getComparator().compare(o1, o2);
//                }
//                // 否则保持原始顺序（按displayId）
//                return Integer.compare(
//                        Integer.parseInt(o1.getDisplayId()),
//                        Integer.parseInt(o2.getDisplayId())
//                );
//            });
//            return true;
//        });
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
