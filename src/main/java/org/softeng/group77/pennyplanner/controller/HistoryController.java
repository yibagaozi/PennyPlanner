package org.softeng.group77.pennyplanner.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;

public class HistoryController {
    @FXML
    private Label date; // 必须与fx:id完全一致
    @FXML
    private ComboBox<Integer> Year;
    @FXML
    private ComboBox<String> Month;
    @FXML
    private ComboBox<String> category; // 绑定 FXML 中的 fx:id="category"
    @FXML
    private TableView<tableModel> transactionTable;
    @FXML
    private TableColumn<tableModel,String> transactionidColumn;
    @FXML
    private TableColumn<tableModel, String> dateColumn;
    @FXML
    private TableColumn<tableModel, String> descriptionColumn;
    @FXML
    private TableColumn<tableModel, Double> amountColumn;
    @FXML
    private TableColumn<tableModel, String> categoryColumn;
    @FXML
    private TableColumn<tableModel, String> methodColumn;
    // 存储表格数据的ObservableList
    private ObservableList<tableModel> transactionData = FXCollections.observableArrayList();
    @FXML
    private void initialize() {

        String d = "March 2025";
        date.setText(d);
        // 初始化年份选择框 (2000-2025)
        for (int year = 2000; year <= 2025; year++) {
            Year.getItems().add(year);
        }
        // 初始化月份选择框 (1-12月)
        for (int month = 1; month <= 12; month++) {
            Month.getItems().add(month + "月");
        }

        // 年份选择变化监听器
        Year.valueProperty().addListener((obs, oldYear, newYear) -> {
            Month.setDisable(newYear == null);
            Month.getSelectionModel().clearSelection();

        });
        // 月份选择变化监听器
        Month.valueProperty().addListener((obs, oldMonth, newMonth) -> {
            printSelectedDate();
        });
        // 设置下拉框的选项
        ObservableList<String> categories = FXCollections.observableArrayList(
                "Food",          // 食品
                "Entertainment", // 娱乐
                "Transportation",// 交通
                "Education",     // 教育
                "Clothes",       // 衣物
                "Others"         // 其他
        );
        category.setItems(categories); // 绑定数据
        // 监听用户选择并打印
        category.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    System.out.println("用户选择了: " + newValue); // 打印到控制台
                }
        );

        // 设置列与模型属性的绑定
        transactionidColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        methodColumn.setCellValueFactory(new PropertyValueFactory<>("method"));

        // 设置表格数据源
        transactionTable.setItems(transactionData);
        addSampleData();
        // 自定义金额列的显示格式（负数显示红色）
        amountColumn.setCellFactory(column -> new javafx.scene.control.TableCell<tableModel, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                    setStyle("");
                } else {
                    // 格式化金额显示（保留两位小数）
                    setText(String.format("$%.2f", amount));
                    // 负数显示为红色
                    if (amount < 0) {
                        setStyle("-fx-text-fill: red;");
                    } else {
                        setStyle("-fx-text-fill: green;");
                    }
                }
            }
        });


    }
    //打印选择的日期
    private void printSelectedDate() {
        Integer selectedYear = Year.getValue();
        String selectedMonth = Month.getValue();

        if (selectedYear != null && selectedMonth != null) {
            // Remove "月" from the month string and convert to number
            String monthNumberStr = selectedMonth.replace("月", "");
            int monthNumber = Integer.parseInt(monthNumberStr);

            System.out.println("Selected Date: " + monthNumber + "/" + selectedYear);
        }
    }
    //增加条目
    public void addTransaction(String id, String date, String description, double amount,
                               String category, String method) {
        tableModel newTransaction = new tableModel(id,date, description, amount, category, method);
        transactionData.add(newTransaction);
    }

    /**
     * 添加示例数据到表格（用于测试）
     */
    public void addSampleData() {
        addTransaction("1", "2023-06-15", "超市购物", -125.50, "食品", "信用卡");
        addTransaction("2","2023-06-16", "工资收入", 5000.00, "收入", "银行转账");
        addTransaction("3","2023-06-17", "水电费", -230.75, "账单", "自动扣款");

    }
    public void removeTransactionByIndex(int index) {
        if (index >= 0 && index < transactionData.size()) {
            transactionData.remove(index);
            System.out.println("删除第"+index+"条");
        }
    }
    /**
     * 清空表格数据
     */
    public void clearTable() {
        transactionData.clear();
    }
    @FXML
    private void turntoHome() throws IOException {
        System.out.println("转到home页面");
        MainApp.showHome();
    }
    @FXML
    private void turntoReport() throws IOException {
        System.out.println("转到home页面");
        MainApp.showReport();
    }@FXML
    private void turntoHistory() throws IOException {
        System.out.println("转到home页面");
        MainApp.showhistory();
    }@FXML
    private void turntoManagement() throws IOException {
        System.out.println("转到home页面");
        MainApp.showmanagement();
    }@FXML
    private void turntoUser() throws IOException {
        System.out.println("转到home页面");
        MainApp.showuser();
    }
    @FXML
    private void turntoLogin() throws IOException {
        System.out.println("Login");
        MainApp.showLogin();
    }

}