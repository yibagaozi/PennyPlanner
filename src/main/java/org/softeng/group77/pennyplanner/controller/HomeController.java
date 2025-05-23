package org.softeng.group77.pennyplanner.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;
import org.softeng.group77.pennyplanner.adapter.TransactionAdapter;
import org.softeng.group77.pennyplanner.exception.BudgetNotFoundException;
import org.softeng.group77.pennyplanner.service.AuthService;
import org.softeng.group77.pennyplanner.model.*;
import org.softeng.group77.pennyplanner.service.BudgetService;
import org.softeng.group77.pennyplanner.service.ChartService;
import org.softeng.group77.pennyplanner.service.ChartViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import java.time.YearMonth;
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
@Slf4j
public class HomeController {
    @FXML
    private Label usernameLabel; // 必须与fx:id完全一致

    @FXML
    private SplitPane splitPane;

    @FXML
    private StackPane expenseTrendChartContainer; // 替换原来的LineChart

    @FXML
    private StackPane expenseDistributionChartContainer; // 替换原来的PieChart

    @FXML
    private Label totalBalanceLabel; // 总余额显示标签

    @FXML
    private Label incomeAmountLabel; // 收入金额显示标签

    @FXML
    private Label expenseAmountLabel; // 支出金额显示标签

    @FXML
    private TextField budgetField; // 预算输入框

    @FXML
    private Button saveBudgetButton; // 保存预算按钮

    @FXML // 新增 ProgressBar 字段
    private ProgressBar budgetProgressBar;

    @Autowired
    private ChartService chartService;
    @Autowired
    private ChartViewService chartViewService;
    @Autowired
    private BudgetService budgetService;
    //数据持久化相关
    private static TransactionAdapter transactionAdapter;
    private static AuthService authService;

    // 用于存储当月财务摘要，避免重复计算
    private MonthlyFinancialSummary currentMonthlySummary;

    public static void setTransactionAdapter(TransactionAdapter adapter) {
        transactionAdapter = adapter;
    }

    public static void setAuthService(AuthService service) {
        authService = service;
    }

    private static ChartService staticChartService;
    private static ChartViewService staticChartViewService;

    @FXML
    private void initialize() throws Exception {
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

        //4. 初始化预算服务
//        setBudgetService(budgetService);
        budgetService = budgetService;
        System.out.println("BudgetService auto-wired successfully");

        // 5. 更新卡片显示
        updateCardInfo();

        // 5. 加载预算信息
        if (budgetService != null) {
            loadBudgetInfo();
        };

        // 6. 设置预算保存按钮事件
        setupSaveBudgetButton();

        // 7. 更新预算进度条
        updateBudgetProgressBar();


        // 禁用分割线的拖动
        splitPane.getDividers().forEach(divider -> divider.positionProperty().addListener((observable, oldValue, newValue) -> {
            divider.setPosition(0.12); // 固定分割线位置为 10%
        }));
    }

    // 设置保存预算的按钮事件
    private void setupSaveBudgetButton() {
        saveBudgetButton.setOnAction(e -> saveBudget());
    }

    // 保存预算
    @FXML
    private void saveBudget() {
        try {
            String budgetText = budgetField.getText();
            if (budgetText != null && !budgetText.isEmpty()) {
                double budgetAmount = Double.parseDouble(budgetText);
                if (budgetService != null) {
                    budgetService.saveBudget(budgetAmount, LocalDate.now());
                    System.out.println("Budget saved: " + budgetAmount);
                    updateBudgetProgressBar();
                } else {
                    System.out.println("BudgetService is not initialized");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid budget amount: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("An I/O error occurred: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
            throw new RuntimeException("Error while saving budget", e);
        }
    }

    // 加载预算信息
    private void loadBudgetInfo() throws Exception {
        try {
            if (budgetService != null) {
                Budget currentBudget = budgetService.getCurrentBudget();
                if (currentBudget != null) {
                    budgetField.setText(String.format("%.2f", currentBudget.getAmount()));
                }
            }
        } catch (BudgetNotFoundException e) {
        log.info("No budget found for current month. Displaying default values.");
        //budgetField.setText("No budget set");
    }
    }

    // 更新卡片信息
    private void updateCardInfo() {
        // 获取当月的收入和支出数据
        MonthlyFinancialSummary summary = calculateMonthlyFinancials();

        // 更新卡片显示
        totalBalanceLabel.setText(String.format("%.2f", summary.getTotalBalance()));
        incomeAmountLabel.setText(String.format("%.2f", summary.getTotalIncome()));
        expenseAmountLabel.setText(String.format("%.2f", Math.abs(summary.getTotalExpense())));
    }

    // 计算当月财务摘要
    private MonthlyFinancialSummary calculateMonthlyFinancials() {
        double totalIncome = 0;
        double totalExpense = 0;

        // 获取当前年月
        YearMonth currentMonth = YearMonth.now();
        LocalDate startOfMonth = currentMonth.atDay(1); // 当月第一天
        LocalDate endOfMonth = currentMonth.atEndOfMonth(); // 当月最后一天

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

        // 筛选当月交易并计算总收入和总支出
        for (Transaction tx : transactions) {
            LocalDate txDate = tx.getTransactionDateTime().toLocalDate();

            // 检查交易是否在当前月内
            if (!txDate.isBefore(startOfMonth) && !txDate.isAfter(endOfMonth)) {
                double amount = tx.getAmountAsDouble();
                if (amount > 0) {
                    totalIncome += amount;
                } else if (amount < 0) {
                    totalExpense += amount; // 支出是负数
                }
            }
        }

        double totalBalance = totalIncome + totalExpense; // 总余额 = 收入 + 支出(负数)

        return new MonthlyFinancialSummary(totalIncome, totalExpense, totalBalance);
    }



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

    @FXML
    private void turntoFinancialAssistant() throws IOException {
        MainApp.showFinancialAssistant();
    }

    //收入趋势图
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
    }

    // 收入分布--饼图
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

    // 月度财务summary内部类
    private static class MonthlyFinancialSummary {
        private final double totalIncome;
        private final double totalExpense;
        private final double totalBalance;

        public MonthlyFinancialSummary(double totalIncome, double totalExpense, double totalBalance) {
            this.totalIncome = totalIncome;
            this.totalExpense = totalExpense;
            this.totalBalance = totalBalance;
        }

        public double getTotalIncome() {
            return totalIncome;
        }

        public double getTotalExpense() {
            return totalExpense;
        }

        public double getTotalBalance() {
            return totalBalance;
        }
    }

    // 更新预算进度条
    private void updateBudgetProgressBar() {
        if (budgetProgressBar == null) {
            log.warn("budgetProgressBar is null. Cannot update progress.");
            return;
        }

        double budgetAmount = 0.0;
        boolean budgetSet = false;

        try {
            if (budgetService != null) {
                Budget currentBudget = budgetService.getCurrentBudget(); // 从服务获取最新预算
                if (currentBudget != null) {
                    budgetAmount = currentBudget.getAmount();
                    budgetSet = true;
                }
            }
        } catch (BudgetNotFoundException e) {
            // 这是正常情况，表示用户未设置预算
            budgetSet = false;
        } catch (Exception e) {
            log.error("Error fetching budget for progress bar: {}", e.getMessage(), e);
            budgetSet = false; // 如果获取预算出错，也视为未设置
        }

        // 如果未设置预算或预算金额无效 (小于等于0)
        if (!budgetSet || budgetAmount <= 0) {
            budgetProgressBar.setProgress(0.0);
            return;
        }

        // 获取当月支出
        double currentMonthExpenses = 0.0;
        MonthlyFinancialSummary summary_1 = calculateMonthlyFinancials();
        if (summary_1 != null) {
            currentMonthExpenses = Math.abs(summary_1.getTotalExpense());
        } else {
            // 如果 currentMonthlySummary 尚未计算（理论上不应发生，因为 updateCardInfo 应该先调用）
            // 或者计算失败，则支出视为0，进度条将显示全部预算剩余。
            // 更健壮的做法是确保 currentMonthlySummary 总是有值，或者在这里重新计算（但可能效率低）。
            log.warn("summary_1 is null when updating progress bar. Expenses assumed to be 0 for progress calculation.");
        }

        double remainingBudget = budgetAmount - currentMonthExpenses;
        double progress;

        if (remainingBudget > 0) {
            progress = remainingBudget / budgetAmount;
        } else {
            // 支出已达到或超过预算
            progress = 0.0;
        }

        // 确保进度值在0到1之间
        progress = Math.max(0.0, Math.min(1.0, progress));

        budgetProgressBar.setProgress(progress);
    }

}
