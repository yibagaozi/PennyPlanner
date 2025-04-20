package org.softeng.group77.pennyplanner.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import org.softeng.group77.pennyplanner.adapter.TransactionAdapter;
import org.softeng.group77.pennyplanner.service.AuthService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class HomeController {
    @FXML
    private Label usernameLabel; // 必须与fx:id完全一致

    @FXML
    private LineChart<String, Number> expenseTrendChart; // 支出趋势折线图

    @FXML
    private PieChart expenseDistributionChart; // 支出分布饼图

    //数据持久化相关
    private static TransactionAdapter transactionAdapter;
    private static AuthService authService;

    public static void setTransactionAdapter(TransactionAdapter adapter) {
        transactionAdapter = adapter;
    }

    public static void setAuthService(AuthService service) {
        authService = service;
    }

    @FXML
    private void initialize() {
        // 1. 设置用户名
        String username = "Guest";
        if (authService != null) {
            try {
                username = authService.getCurrentUser().getUsername();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        usernameLabel.setText("Welcome back, " + username);

        // 2. 初始化支出趋势折线图
        setupExpenseTrendChart();

        // 3. 初始化支出分布饼图
        setupExpenseDistributionChart();

    }
    @FXML
    private void turntoHome() throws IOException {
        //System.out.println("转到home页面");
        MainApp.showHome();
    }
    @FXML
    private void turntoReport() throws IOException {
       // System.out.println("转到home页面");
        MainApp.showReport();
    }@FXML
    private void turntoHistory() throws IOException {
       // System.out.println("转到home页面");
        MainApp.showhistory();
    }@FXML
    private void turntoManagement() throws IOException {
       // System.out.println("转到home页面");
        MainApp.showmanagement();
    }@FXML
    private void turntoUser() throws IOException {
      //  System.out.println("转到home页面");
        MainApp.showuser();
    }
    @FXML
    private void turntoLogin() throws IOException {
        System.out.println("Login");
        MainApp.showLogin();
    }
    private void setupExpenseTrendChart() {
        // 创建数据系列
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Daily Expense");

        if (transactionAdapter != null) {
            // 获取最近30天的支出数据
            List<TransactionAdapter.DateSum> dailyExpenses = transactionAdapter.getDailyExpenseSummary(30);

            // 转换为图表数据点
            for (TransactionAdapter.DateSum dateSum : dailyExpenses) {
                series.getData().add(new XYChart.Data<>(dateSum.getDate(), dateSum.getAmount()));
            }
        } else{
            // 添加示例数据
            series.getData().add(new XYChart.Data<>("23 Mar", 5000));
            series.getData().add(new XYChart.Data<>("24", 8000));
            series.getData().add(new XYChart.Data<>("25", 12000));
            series.getData().add(new XYChart.Data<>("26", 15000));
            series.getData().add(new XYChart.Data<>("27", 18000));
            series.getData().add(new XYChart.Data<>("28", 22000));
            series.getData().add(new XYChart.Data<>("29", 26000));
            series.getData().add(new XYChart.Data<>("30", 30000));
        }



        // 将系列添加到折线图
        expenseTrendChart.getData().add(series);

        // 设置图表样式
        expenseTrendChart.setLegendVisible(false);

        // 设置线条颜色为黑色(与图片一致)
        for (XYChart.Data<String, Number> data : series.getData()) {
            data.getNode().setStyle("-fx-background-color: black, white;");
        }
    }

    private void setupExpenseDistributionChart() {
//        // 创建饼图数据(与图片中的数值一致)
//        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
//                new PieChart.Data("Food", 237),
//                new PieChart.Data("Entertainment", 99),
//                new PieChart.Data("Education", 479),
//                new PieChart.Data("Transportation", 345),
//                new PieChart.Data("Others", 608)
//        );
        // 创建饼图数据
        ObservableList<PieChart.Data> pieData;
        if (transactionAdapter != null) {
            // 获取类别统计数据
            List<TransactionAdapter.CategorySum> categorySummary = transactionAdapter.getCategorySummary();

            // 转换为饼图数据
            pieData = FXCollections.observableArrayList(
                    categorySummary.stream()
                            .map(sum -> new PieChart.Data(sum.getCategory(), sum.getAmount()))
                            .collect(Collectors.toList())
            );

            // 如果没有数据，添加示例数据
            if (pieData.isEmpty()) {
                pieData = getDefaultPieChartData();
            }
        } else {
            // 如果还没有初始化适配器，添加示例数据
            pieData = getDefaultPieChartData();
        }

        expenseDistributionChart.setData(pieData);

        // 设置饼图颜色(与图片风格一致)
        int i = 0;
        String[] colors = {
                "-fx-pie-color: #e74c3c;",  // Food - 红色
                "-fx-pie-color: #f1c40f;",  // Entertainment - 黄色
                "-fx-pie-color: #2ecc71;",  // Education - 绿色
                "-fx-pie-color: #3498db;",  // Transportation - 蓝色
                "-fx-pie-color: #9b59b6;"   // Others - 紫色
        };

        for (PieChart.Data data : pieData) {
            data.getNode().setStyle(colors[i]);
            i++;
        }

        // 显示数值标签
        expenseDistributionChart.setLabelsVisible(true);


    }
    private ObservableList<PieChart.Data> getDefaultPieChartData() {
        return FXCollections.observableArrayList(
                new PieChart.Data("Food", 237),
                new PieChart.Data("Entertainment", 99),
                new PieChart.Data("Education", 479),
                new PieChart.Data("Transportation", 345),
                new PieChart.Data("Others", 608)
        );
    }
}
