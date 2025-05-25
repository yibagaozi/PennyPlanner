package org.softeng.group77.pennyplanner.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.softeng.group77.pennyplanner.model.*;
import org.softeng.group77.pennyplanner.service.ChartService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ChartServiceImpl class.
 * These tests verify the chart generation functionality provided by the ChartService,
 * including different chart types (pie, line, bar) and data aggregation methods.
 * The tests use a predefined set of transaction data spanning multiple months and
 * categories to validate the visualization outputs.
 *
 * @author XI Yu
 * @version 2.0.0
 * @since 1.1.0
 */
public class ChartServiceImplTest {

    private ChartService chartService;
    private List<Transaction> testTransactions;

    /**
     * Sets up the test environment before each test method.
     * Initializes the ChartService and creates test transaction data.
     */
    @BeforeEach
    void setUp() {
        // 初始化服务实例
        chartService = new ChartServiceImpl();

        // 准备测试数据
        testTransactions = createTestTransactions();
    }

    /**
     * Creates test transaction data spanning multiple months and categories.
     *
     * @return a list of transaction objects for testing
     */
    private List<Transaction> createTestTransactions() {
        List<Transaction> transactions = new ArrayList<>();

        // 添加2025年1月的测试交易
        transactions.add(new Transaction("Shopping", "Food", new BigDecimal("-120.50"), "Alipay",
                LocalDateTime.of(2025, 1, 5, 14, 30), "user1"));

        transactions.add(new Transaction("Gas", "Transportation", new BigDecimal("-200.00"), "Cash",
                LocalDateTime.of(2025, 1, 10, 10, 15), "user1"));

        transactions.add(new Transaction("Movie", "Entertainment", new BigDecimal("-80.00"), "Credit Card",
                LocalDateTime.of(2025, 1, 15, 19, 0), "user1"));

        transactions.add(new Transaction("Rent", "Living Bill", new BigDecimal("-1500.00"), "Cash",
                LocalDateTime.of(2025, 1, 20, 9, 0), "user1"));

        // 添加2025年2月的测试交易
        transactions.add(new Transaction("Dinner", "Food", new BigDecimal("-150.75"), "Cash",
                LocalDateTime.of(2025, 2, 5, 20, 30), "user1"));

        transactions.add(new Transaction("Subway", "Transportation", new BigDecimal("-150.00"), "Alipay",
                LocalDateTime.of(2025, 2, 10, 8, 15), "user1"));

        transactions.add(new Transaction("Singing", "Entertainment", new BigDecimal("-300.00"), "Credit Card",
                LocalDateTime.of(2025, 2, 15, 19, 30), "user1"));

        transactions.add(new Transaction("Rent", "Living Bill", new BigDecimal("-1500.00"), "WeChat Pay",
                LocalDateTime.of(2025, 2, 20, 9, 0), "user1"));

        // 添加2025年3月的测试交易
        transactions.add(new Transaction("Shopping", "Food", new BigDecimal("-180.25"), "Alipay",
                LocalDateTime.of(2025, 3, 5, 15, 45), "user1"));

        transactions.add(new Transaction("Car", "Transportation", new BigDecimal("-500.00"), "Credit Card",
                LocalDateTime.of(2025, 3, 10, 14, 0), "user1"));

        transactions.add(new Transaction("Game", "Entertainment", new BigDecimal("-60.00"), "Cash",
                LocalDateTime.of(2025, 3, 15, 22, 0), "user1"));

        transactions.add(new Transaction("Rent", "Living Bill", new BigDecimal("-1500.00"), "WeChat Pay",
                LocalDateTime.of(2025, 3, 20, 9, 0), "user1"));

        // 添加一些2024年的测试交易
        transactions.add(new Transaction("Shopping", "Food", new BigDecimal("-100.00"), "Alipay",
                LocalDateTime.of(2024, 12, 5, 14, 30), "user1"));

        transactions.add(new Transaction("Gas", "Transportation", new BigDecimal("-180.00"), "Credit Card",
                LocalDateTime.of(2024, 12, 10, 10, 15), "user1"));

        // 添加一个空类别的交易
        transactions.add(new Transaction("Others", null, new BigDecimal("-50.00"), "WeChat Pay",
                LocalDateTime.of(2025, 1, 25, 12, 0), "user1"));

        return transactions;
    }

    /**
     * Tests pie chart generation for expense category distribution.
     */
    @Test
    @DisplayName("Test Pie Chart for Category Distribution")
    void testGenerateCategoryDistributionPieChart() {
        // 执行方法
        PieChartData pieChartData = chartService.generateCategoryDistributionPieChart(testTransactions, "Test pie chart");

        // 验证结果
        assertNotNull(pieChartData, "Pie chart data should not be null");
        assertEquals("Test pie chart", pieChartData.getTitle(), "Title should match");
        assertTrue(pieChartData.isShowLegend(), "Should show legend");
        assertTrue(pieChartData.isAnimated(), "Should use animation");

        // 验证数据点
        List<ChartDataPoint> dataPoints = pieChartData.getDataPoints();
        assertNotNull(dataPoints, "Data points should not be null");
        /*
        assertFalse(dataPoints.isEmpty(), "数据点列表不应为空");

        // 验证分类计算是否正确
        boolean hasFood = false;
        boolean hasTransport = false;
        boolean hasHousing = false;

        for (ChartDataPoint point : dataPoints) {
            if ("Food".equals(point.getLabel())) {
                hasFood = true;
                // 1月: 120.50 + 2月: 150.75 + 3月: 180.25 = 451.50
                assertEquals(551.50, point.getValue(), 0.01, "Food类别总额应正确");
            }
            if ("Transportation".equals(point.getLabel())) {
                hasTransport = true;
                // 1月: 200.00 + 2月: 150.00 + 3月: 500.00 = 850.00
                assertEquals(1030.00, point.getValue(), 0.01, "Transportation类别总额应正确");
            }
            if ("Living Bill".equals(point.getLabel())) {
                hasHousing = true;
                // 1月: 1500.00 + 2月: 1500.00 + 3月: 1500.00 = 4500.00
                assertEquals(4500.00, point.getValue(), 0.01, "Living Bill类别总额应正确");
            }
        }

        assertTrue(hasFood, "应包含Food类别");
        assertTrue(hasTransport, "应包含Transportation类别");
        assertTrue(hasHousing, "应包含Living Bill类别");
        */
    }

    /**
     * Tests pie chart generation with an empty transaction list.
     */
    @Test
    @DisplayName("Test Pie Chart with Empty Transactions")
    void testGeneratePieChartWithEmptyTransactions() {
        // 使用空列表
        PieChartData pieChartData = chartService.generateCategoryDistributionPieChart(new ArrayList<>(), "Empty Test");

        // 验证结果
        assertNotNull(pieChartData, "Null pie chart data should not be returned");
        assertEquals("Empty Test", pieChartData.getTitle(), "Title should match");
        assertTrue(pieChartData.getDataPoints().isEmpty(), "Data points should be empty for no transactions");
    }

    /**
     * Tests monthly expense line chart generation for a specific year.
     */
    @Test
    @DisplayName("Test Monthly Expense Line Chart Generation")
    void testGenerateMonthlyExpenseLineChart() {
        // 执行方法
        TimeSeriesData lineChartData = chartService.generateMonthlyExpenseLineChart(testTransactions, 2025, "Monthly Expense Test");

        // 验证结果
        assertNotNull(lineChartData, "Line chart data should not be null");
        assertEquals("Monthly Expense Test", lineChartData.getTitle(), "Title should match");
        assertEquals("Month", lineChartData.getXAxisLabel(), "X axis label should be 'Month'");
        assertEquals("Amount", lineChartData.getYAxisLabel(), "Y axis label should be 'Amount'");

        // 验证数据系列
        Map<String, List<ChartDataPoint>> series = lineChartData.getSeries();
        assertNotNull(series, "Data series should not be null");
        assertTrue(series.containsKey("Monthly Expense"), "Should contain 'Monthly Expense' series");

        List<ChartDataPoint> monthlyPoints = series.get("Monthly Expense");
        assertNotNull(monthlyPoints, "Monthly points should not be null");
        assertEquals(12, monthlyPoints.size(), "Should have 12 months of data points");

        // 验证特定月份的金额
        for (ChartDataPoint point : monthlyPoints) {
            if ("01月".equals(point.getLabel())) {
                // 1月总额: 120.50 + 200.00 + 80.00 + 1500.00 + 50.00 = 1950.50
                assertEquals(1950.50, point.getValue(), 0.01, "Total for January should be correct");
            }
            if ("02月".equals(point.getLabel())) {
                // 2月总额: 150.75 + 150.00 + 300.00 + 1500.00 = 2100.75
                assertEquals(2100.75, point.getValue(), 0.01, "Total for February should be correct");
            }
            if ("03月".equals(point.getLabel())) {
                // 3月总额: 180.25 + 500.00 + 60.00 + 1500.00 = 2240.25
                assertEquals(2240.25, point.getValue(), 0.01, "Total for March should be correct");
            }
        }
    }

    /**
     * Tests monthly expense line chart generation for different years.
     *
     * @param year the year to generate the chart for
     */
    @ParameterizedTest
    @ValueSource(ints = {2024, 2025, 2026})
    @DisplayName("Test Monthly Expense Line Chart for Different Years")
    void testGenerateMonthlyExpenseLineChartDifferentYears(int year) {
        // 执行方法
        TimeSeriesData lineChartData = chartService.generateMonthlyExpenseLineChart(testTransactions, year, "Yearly Test");

        // 验证结果
        assertNotNull(lineChartData, "Line chart data should not be null");
        assertEquals("Yearly Test", lineChartData.getTitle(), "Title should match");

        // 验证数据系列
        Map<String, List<ChartDataPoint>> series = lineChartData.getSeries();
        List<ChartDataPoint> monthlyPoints = series.get("Monthly Expense");

        // 2024应只有12月有数据，2025应有1-3月有数据，2026应无数据
        if (year == 2024) {
            for (ChartDataPoint point : monthlyPoints) {
                if ("12月".equals(point.getLabel())) {
                    // 12月总额: 100.00 + 180.00 = 280.00
                    assertEquals(280.00, point.getValue(), 0.01, "Budget for December 2024 should be correct");
                }
            }
        } else if (year == 2025) {
            // 已在前面的测试中验证
        } else if (year == 2026) {
            // 验证所有月份都是零值
            for (ChartDataPoint point : monthlyPoints) {
                assertEquals(0.0, point.getValue(), 0.01, "All months in 2026 should have zero value");
            }
        }
    }

    /**
     * Tests category comparison bar chart generation.
     */
    @Test
    @DisplayName("Test Category Comparison Bar Chart Generation")
    void testGenerateCategoryComparisonBarChart() {
        // 准备指定分类列表
        List<String> categories = Arrays.asList("Food", "Transportation", "Entertainment", "Living Bill");

        // 执行方法
        CategoryChartData barChartData = chartService.generateCategoryComparisonBarChart(
                testTransactions, categories, "Category Comparison Test");

        // 验证结果
        assertNotNull(barChartData, "Bar chart data should not be null");
        assertEquals("Category Comparison Test", barChartData.getTitle(), "Title should match");
        assertEquals("Category", barChartData.getXAxisLabel(), "X axis label should be 'Category'");
        assertEquals("Amount", barChartData.getYAxisLabel(), "Y axis label should be 'Amount'");

        // 验证分类数据
        assertEquals(categories, barChartData.getCategories(), "Categories should match the specified list");

        // 验证系列数据
        Map<String, List<Double>> seriesData = barChartData.getSeries();
        assertNotNull(seriesData, "Series data should not be null");
        assertTrue(seriesData.containsKey("Amount"), "Should contain 'Amount' series");

        List<Double> values = seriesData.get("Amount");
        assertNotNull(values, "Double values should not be null");
        assertEquals(4, values.size(), "Should have 4 categories");

        // 验证每个分类的总额
        assertEquals(551.50, values.get(0), 0.01, "Food category total should be correct");
        assertEquals(1030.00, values.get(1), 0.01, "Transportation category total should be correct");
        assertEquals(440.00, values.get(2), 0.01, "Entertainment category total should be correct");
        assertEquals(4500.00, values.get(3), 0.01, "Living category total should be correct");
    }

    /**
     * Tests bar chart generation without specifying categories.
     */
    @Test
    @DisplayName("Test Category Comparison Bar Chart with Empty Transactions")
    void testGenerateCategoryBarChartWithoutCategories() {
        // 执行方法 - 不指定分类列表
        CategoryChartData barChartData = chartService.generateCategoryComparisonBarChart(
                testTransactions, null, "All Categories Comparison Test");

        // 验证结果
        assertNotNull(barChartData, "Bar chart data should not be null");
        List<String> extractedCategories = barChartData.getCategories();
        assertNotNull(extractedCategories, "Categories should not be null");

        // 验证是否提取了所有不同的分类
        assertTrue(extractedCategories.contains("Food"), "Should contain Food category");
        assertTrue(extractedCategories.contains("Transportation"), "Should contain Transportation category");
        assertTrue(extractedCategories.contains("Entertainment"), "Should contain Entertainment category");
        assertTrue(extractedCategories.contains("Living Bill"), "Should contain Living Bill category");
    }

    /**
     * Tests expense chart generation for different time periods.
     *
     * @param period the time period granularity to test
     */
    @ParameterizedTest
    @EnumSource(TimeSeriesData.ChartPeriod.class)
    @DisplayName("测试不同时间周期的日期范围支出图")
    void testGenerateDateRangeExpenseChart(TimeSeriesData.ChartPeriod period) {
        // 设置日期范围
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 3, 31);

        // 执行方法
        TimeSeriesData chartData = chartService.generateDateRangeExpenseChart(
                testTransactions, startDate, endDate, period);

        // 验证结果
        assertNotNull(chartData, "Chart data should not be null");
        assertEquals(period, chartData.getPeriod(), "Period should match the input");

        // 验证数据系列
        Map<String, List<ChartDataPoint>> series = chartData.getSeries();
        assertNotNull(series, "Data series should not be null");
        assertTrue(series.containsKey("Expense Trend"), "Should contain 'Expense Trend' series");

        // 验证至少有一个数据点
        List<ChartDataPoint> dataPoints = series.get("Expense Trend");
        assertFalse(dataPoints.isEmpty(), "Data points should not be empty");

        // 根据不同周期验证数据点数量和格式
        switch (period) {
            case DAILY:
                // 不需要精确验证每天，但应确保有正确格式的数据点
                for (ChartDataPoint point : dataPoints) {
                    assertTrue(point.getLabel().matches("\\d{2}-\\d{2}"),
                            "Daily data point label format should be DD-MM");
                }
                break;
            case WEEKLY:
                // 验证周数据点格式
                for (ChartDataPoint point : dataPoints) {
                    assertTrue(point.getLabel().matches("\\d{4}-W\\d{1,2}"),
                            "Weekly data point label format should be YYYY-WW");
                }
                break;
            case MONTHLY:
                // 验证月数据点
                assertEquals(3, dataPoints.size(), "Should have 3 monthly data points");
                for (ChartDataPoint point : dataPoints) {
                    assertTrue(point.getLabel().matches("\\d{4}-\\d{2}"),
                            "Monthly data point label format should be YYYY-MM");
                }
                break;
            case YEARLY:
                // 验证年数据点 - 只应有2025年
                assertEquals(1, dataPoints.size(), "Should have 1 year data point");
                assertEquals("2025", dataPoints.get(0).getLabel(), "Year label should be 2025");
                // 2025年1-3月总额: 1950.50 + 2100.75 + 2240.25 = 6291.50
                assertEquals(6291.50, dataPoints.get(0).getValue(), 0.01, "Total for 2025 should be correct");
                break;
        }
    }

    /**
     * Tests multi-category time series chart generation.
     */
    @Test
    @DisplayName("Test Multi-Category Time Series Chart Generation")
    void testGenerateMultiCategoryTimeSeriesChart() {
        // 指定特定分类
        List<String> categories = Arrays.asList("Food", "Transportation", "Living Bill");

        // 执行方法
        TimeSeriesData chartData = chartService.generateMultiCategoryTimeSeriesChart(
                testTransactions, 2025, categories);

        // 验证结果
        assertNotNull(chartData, "Chart data should not be null");
        assertEquals("2025yearly expense trend by category", chartData.getTitle(), "Title should match");

        // 验证数据系列
        Map<String, List<ChartDataPoint>> series = chartData.getSeries();
        assertNotNull(series, "Data series should not be null");
        assertEquals(3, series.size(), "Should have 3 series for specified categories");

        // 验证每个分类的系列存在
        assertTrue(series.containsKey("Food"), "Should contain Food series");
        assertTrue(series.containsKey("Transportation"), "Should contain Transportation series");
        assertTrue(series.containsKey("Living Bill"), "Should contain Living Bill series");

        // 验证每个系列的数据点
        for (String category : categories) {
            List<ChartDataPoint> points = series.get(category);
            assertNotNull(points, category + " series data should not be null");
            assertEquals(12, points.size(), category + " should have 12 data points for each month");
        }

        // 验证特定月份和分类的值
        for (ChartDataPoint point : series.get("Food")) {
            if ("01月".equals(point.getLabel())) {
                assertEquals(120.50, point.getValue(), 0.01, "Food expense for Jan should correct");
            } else if ("02月".equals(point.getLabel())) {
                assertEquals(150.75, point.getValue(), 0.01, "Food expense for Feb should correct");
            } else if ("03月".equals(point.getLabel())) {
                assertEquals(180.25, point.getValue(), 0.01, "Food expense for Mar should correct");
            }
        }
    }

    /**
     * Tests multi-category chart generation without specifying categories.
     */
    @Test
    @DisplayName("Test Multi-Category Chart Without Specified Categories")
    void testGenerateMultiCategoryChartWithoutCategories() {
        // 执行方法 - 不指定分类列表
        TimeSeriesData chartData = chartService.generateMultiCategoryTimeSeriesChart(
                testTransactions, 2025, null);

        // 验证结果
        assertNotNull(chartData, "Chart data should not be null");

        // 验证数据系列 - 应自动提取所有分类
        Map<String, List<ChartDataPoint>> series = chartData.getSeries();
        assertNotNull(series, "Data series should not be null");

        // 验证是否提取了所有不同的分类
        assertTrue(series.containsKey("Food"), "Should contain Food series");
        assertTrue(series.containsKey("Transportation"), "Should contain Transportation series");
        assertTrue(series.containsKey("Entertainment"), "Should contain Entertainment series");
        assertTrue(series.containsKey("Living Bill"), "Should contain Living Bill series");
    }
}
