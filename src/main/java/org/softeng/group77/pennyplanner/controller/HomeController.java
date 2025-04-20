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
import org.softeng.group77.pennyplanner.model.*;
import org.softeng.group77.pennyplanner.service.ChartService;
import org.softeng.group77.pennyplanner.service.ChartViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javafx.scene.layout.StackPane;


import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class HomeController {
    @FXML
    private Label usernameLabel; // 必须与fx:id完全一致

//    @FXML
//    private LineChart<String, Number> expenseTrendChart; // 支出趋势折线图
    @FXML
    private StackPane expenseTrendChartContainer; // 替换原来的LineChart

//    @FXML
//    private PieChart expenseDistributionChart; // 支出分布饼图
    @FXML
    private StackPane expenseDistributionChartContainer; // 替换原来的PieChart

    @Autowired
    private ChartService chartService;
    @Autowired
    private ChartViewService chartViewService;


    //数据持久化相关
    private static TransactionAdapter transactionAdapter;
    private static AuthService authService;

    public static void setTransactionAdapter(TransactionAdapter adapter) {
        transactionAdapter = adapter;
    }

    public static void setAuthService(AuthService service) {
        authService = service;
    }

    private static ChartService staticChartService;
    private static ChartViewService staticChartViewService;

    public static void setChartService(ChartService service) {
        staticChartService = service;
    }

    public static void setChartViewService(ChartViewService service) {
        staticChartViewService = service;
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

        // 添加静态服务赋值
        if (staticChartService != null) {
            this.chartService = staticChartService;
        }

        if (staticChartViewService != null) {
            this.chartViewService = staticChartViewService;
        }

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
        // 清除现有内容
        expenseTrendChartContainer.getChildren().clear();

        // 获取交易数据
        List<Transaction> transactions = new ArrayList<>();
        if (transactionAdapter != null) {
            // 使用getUserTransactions方法获取数据，并转换为Transaction对象
            ObservableList<tableModel> models = transactionAdapter.getUserTransactions();
            transactions = models.stream()
                    .map(this::convertToTransaction)
                    .collect(Collectors.toList());
        } else {
            // 模拟数据
            transactions = createSampleTransactions();
        }

        // 获取当前年份
        int currentYear = LocalDate.now().getYear();

        // 生成月度支出图表数据
        TimeSeriesData timeSeriesData = chartService.generateMonthlyExpenseLineChart(
                transactions, currentYear, "Monthly Expense Trend"
        );

        // 创建图表并添加到容器
        LineChart<String, Number> lineChart = chartViewService.createLineChart(timeSeriesData);
        expenseTrendChartContainer.getChildren().add(lineChart);

        // 设置图表占满容器空间
        lineChart.prefWidthProperty().bind(expenseTrendChartContainer.widthProperty());
        lineChart.prefHeightProperty().bind(expenseTrendChartContainer.heightProperty());
//        // 创建数据系列
//        XYChart.Series<String, Number> series = new XYChart.Series<>();
//        series.setName("Daily Expense");
//
//        if (transactionAdapter != null) {
//            // 获取最近30天的支出数据
//            List<TransactionAdapter.DateSum> dailyExpenses = transactionAdapter.getDailyExpenseSummary(30);
//
//            // 转换为图表数据点
//            for (TransactionAdapter.DateSum dateSum : dailyExpenses) {
//                series.getData().add(new XYChart.Data<>(dateSum.getDate(), dateSum.getAmount()));
//            }
//        } else{
//            // 添加示例数据
//            series.getData().add(new XYChart.Data<>("23 Mar", 5000));
//            series.getData().add(new XYChart.Data<>("24", 8000));
//            series.getData().add(new XYChart.Data<>("25", 12000));
//            series.getData().add(new XYChart.Data<>("26", 15000));
//            series.getData().add(new XYChart.Data<>("27", 18000));
//            series.getData().add(new XYChart.Data<>("28", 22000));
//            series.getData().add(new XYChart.Data<>("29", 26000));
//            series.getData().add(new XYChart.Data<>("30", 30000));
//        }
//
//
//
//        // 将系列添加到折线图
//        expenseTrendChart.getData().add(series);
//
//        // 设置图表样式
//        expenseTrendChart.setLegendVisible(false);
//
//        // 设置线条颜色为黑色(与图片一致)
//        for (XYChart.Data<String, Number> data : series.getData()) {
//            data.getNode().setStyle("-fx-background-color: black, white;");
//        }
    }

    private void setupExpenseDistributionChart() {
        // 清除现有内容
        expenseDistributionChartContainer.getChildren().clear();

        // 获取交易数据
        List<Transaction> transactions = new ArrayList<>();
        if (transactionAdapter != null) {
            // 使用getUserTransactions方法获取数据，并转换为Transaction对象
            ObservableList<tableModel> models = transactionAdapter.getUserTransactions();
            transactions = models.stream()
                    .map(this::convertToTransaction)
                    .collect(Collectors.toList());
        } else {
            // 模拟数据
            transactions = createSampleTransactions();
        }

        // 生成饼图数据
        PieChartData pieChartData = chartService.generateCategoryDistributionPieChart(
                transactions, "Expense Distribution by Category"
        );

        // 创建饼图并添加到容器
        PieChart pieChart = chartViewService.createPieChart(pieChartData);
        expenseDistributionChartContainer.getChildren().add(pieChart);

        // 设置图表占满容器空间
        pieChart.prefWidthProperty().bind(expenseDistributionChartContainer.widthProperty());
        pieChart.prefHeightProperty().bind(expenseDistributionChartContainer.heightProperty());
    }

    // 从tableModel转换为Transaction对象
    private Transaction convertToTransaction(tableModel model) {
        Transaction tx = new Transaction();
        tx.setDescription(model.getDescription());
        tx.setAmount(new java.math.BigDecimal(model.getAmount()));
        tx.setCategory(model.getCategory());

        // 转换日期字符串为LocalDateTime
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate date = LocalDate.parse(model.getDate(), formatter);
            tx.setTransactionDateTime(date.atStartOfDay());
        } catch (Exception e) {
            // 如果日期格式有问题，使用当前时间
            tx.setTransactionDateTime(LocalDateTime.now());
        }

        return tx;
    }

    // 创建示例交易数据
    private List<Transaction> createSampleTransactions() {
        List<Transaction> samples = new ArrayList<>();

        // 添加食品类别交易
        samples.add(createTransaction("食品购买", -120.5, "Food", LocalDate.now().minusDays(5)));
        samples.add(createTransaction("餐厅晚餐", -85.5, "Food", LocalDate.now().minusDays(3)));
        samples.add(createTransaction("超市购物", -31.0, "Food", LocalDate.now().minusDays(1)));

        // 添加娱乐类别交易
        samples.add(createTransaction("电影票", -60.0, "Entertainment", LocalDate.now().minusDays(6)));
        samples.add(createTransaction("游戏订阅", -39.0, "Entertainment", LocalDate.now().minusDays(15)));

        // 添加教育类别交易
        samples.add(createTransaction("书籍购买", -152.0, "Education", LocalDate.now().minusDays(10)));
        samples.add(createTransaction("在线课程", -327.0, "Education", LocalDate.now().minusDays(20)));

        // 添加交通类别交易
        samples.add(createTransaction("地铁费用", -45.0, "Transportation", LocalDate.now().minusDays(2)));
        samples.add(createTransaction("打车费用", -120.0, "Transportation", LocalDate.now().minusDays(4)));
        samples.add(createTransaction("公共汽车", -20.0, "Transportation", LocalDate.now().minusDays(1)));
        samples.add(createTransaction("高铁票", -160.0, "Transportation", LocalDate.now().minusDays(12)));

        // 添加其他类别交易
        samples.add(createTransaction("水电煤", -380.0, "Others", LocalDate.now().minusDays(8)));
        samples.add(createTransaction("手机费", -128.0, "Others", LocalDate.now().minusDays(18)));
        samples.add(createTransaction("健身房", -100.0, "Others", LocalDate.now().minusDays(25)));

        return samples;
    }


    // 创建单个交易记录帮助方法
    private Transaction createTransaction(String description, double amount, String category, LocalDate date) {
        Transaction tx = new Transaction();
        tx.setDescription(description);
        tx.setAmount(new java.math.BigDecimal(amount));
        tx.setCategory(category);
        tx.setTransactionDateTime(date.atStartOfDay());
        return tx;
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
