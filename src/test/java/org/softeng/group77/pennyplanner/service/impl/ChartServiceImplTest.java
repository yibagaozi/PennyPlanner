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

public class ChartServiceImplTest {

    private ChartService chartService;
    private List<Transaction> testTransactions;

    @BeforeEach
    void setUp() {
        // 初始化服务实例
        chartService = new ChartServiceImpl();

        // 准备测试数据
        testTransactions = createTestTransactions();
    }

    /**
     * 创建测试用的交易数据
     */
    private List<Transaction> createTestTransactions() {
        List<Transaction> transactions = new ArrayList<>();

        // 添加2025年1月的测试交易
        transactions.add(new Transaction("超市购物", "食品", new BigDecimal("120.50"),
                LocalDateTime.of(2025, 1, 5, 14, 30), "user1"));

        transactions.add(new Transaction("加油站", "交通", new BigDecimal("200.00"),
                LocalDateTime.of(2025, 1, 10, 10, 15), "user1"));

        transactions.add(new Transaction("电影票", "娱乐", new BigDecimal("80.00"),
                LocalDateTime.of(2025, 1, 15, 19, 0), "user1"));

        transactions.add(new Transaction("房租", "住房", new BigDecimal("1500.00"),
                LocalDateTime.of(2025, 1, 20, 9, 0), "user1"));

        // 添加2025年2月的测试交易
        transactions.add(new Transaction("餐厅晚餐", "食品", new BigDecimal("150.75"),
                LocalDateTime.of(2025, 2, 5, 20, 30), "user1"));

        transactions.add(new Transaction("地铁月卡", "交通", new BigDecimal("150.00"),
                LocalDateTime.of(2025, 2, 10, 8, 15), "user1"));

        transactions.add(new Transaction("演唱会", "娱乐", new BigDecimal("300.00"),
                LocalDateTime.of(2025, 2, 15, 19, 30), "user1"));

        transactions.add(new Transaction("房租", "住房", new BigDecimal("1500.00"),
                LocalDateTime.of(2025, 2, 20, 9, 0), "user1"));

        // 添加2025年3月的测试交易
        transactions.add(new Transaction("超市购物", "食品", new BigDecimal("180.25"),
                LocalDateTime.of(2025, 3, 5, 15, 45), "user1"));

        transactions.add(new Transaction("汽车维修", "交通", new BigDecimal("500.00"),
                LocalDateTime.of(2025, 3, 10, 14, 0), "user1"));

        transactions.add(new Transaction("游戏订阅", "娱乐", new BigDecimal("60.00"),
                LocalDateTime.of(2025, 3, 15, 22, 0), "user1"));

        transactions.add(new Transaction("房租", "住房", new BigDecimal("1500.00"),
                LocalDateTime.of(2025, 3, 20, 9, 0), "user1"));

        // 添加一些2024年的测试交易
        transactions.add(new Transaction("超市购物", "食品", new BigDecimal("100.00"),
                LocalDateTime.of(2024, 12, 5, 14, 30), "user1"));

        transactions.add(new Transaction("加油站", "交通", new BigDecimal("180.00"),
                LocalDateTime.of(2024, 12, 10, 10, 15), "user1"));

        // 添加一个空类别的交易
        transactions.add(new Transaction("其他支出", null, new BigDecimal("50.00"),
                LocalDateTime.of(2025, 1, 25, 12, 0), "user1"));

        return transactions;
    }

    @Test
    @DisplayName("测试生成饼图数据 - 按类别分组")
    void testGenerateCategoryDistributionPieChart() {
        // 执行方法
        PieChartData pieChartData = chartService.generateCategoryDistributionPieChart(testTransactions, "测试饼图");

        // 验证结果
        assertNotNull(pieChartData, "饼图数据不应为null");
        assertEquals("测试饼图", pieChartData.getTitle(), "标题应匹配");
        assertTrue(pieChartData.isShowLegend(), "应显示图例");
        assertTrue(pieChartData.isAnimated(), "应启用动画");

        // 验证数据点
        List<ChartDataPoint> dataPoints = pieChartData.getDataPoints();
        assertNotNull(dataPoints, "数据点列表不应为null");
        assertFalse(dataPoints.isEmpty(), "数据点列表不应为空");

        // 验证分类计算是否正确
        boolean hasFood = false;
        boolean hasTransport = false;
        boolean hasHousing = false;

        for (ChartDataPoint point : dataPoints) {
            if ("食品".equals(point.getLabel())) {
                hasFood = true;
                // 1月: 120.50 + 2月: 150.75 + 3月: 180.25 = 451.50
                assertEquals(551.50, point.getValue(), 0.01, "食品类别总额应正确");
            }
            if ("交通".equals(point.getLabel())) {
                hasTransport = true;
                // 1月: 200.00 + 2月: 150.00 + 3月: 500.00 = 850.00
                assertEquals(1030.00, point.getValue(), 0.01, "交通类别总额应正确");
            }
            if ("住房".equals(point.getLabel())) {
                hasHousing = true;
                // 1月: 1500.00 + 2月: 1500.00 + 3月: 1500.00 = 4500.00
                assertEquals(4500.00, point.getValue(), 0.01, "住房类别总额应正确");
            }
        }

        assertTrue(hasFood, "应包含食品类别");
        assertTrue(hasTransport, "应包含交通类别");
        assertTrue(hasHousing, "应包含住房类别");
    }

    @Test
    @DisplayName("测试生成饼图 - 空交易列表")
    void testGeneratePieChartWithEmptyTransactions() {
        // 使用空列表
        PieChartData pieChartData = chartService.generateCategoryDistributionPieChart(new ArrayList<>(), "空测试");

        // 验证结果
        assertNotNull(pieChartData, "即使没有交易，也应返回有效对象");
        assertEquals("空测试", pieChartData.getTitle(), "标题应匹配");
        assertTrue(pieChartData.getDataPoints().isEmpty(), "数据点列表应为空");
    }

    @Test
    @DisplayName("测试生成月度支出折线图")
    void testGenerateMonthlyExpenseLineChart() {
        // 执行方法
        TimeSeriesData lineChartData = chartService.generateMonthlyExpenseLineChart(testTransactions, 2025, "月度支出测试");

        // 验证结果
        assertNotNull(lineChartData, "折线图数据不应为null");
        assertEquals("月度支出测试", lineChartData.getTitle(), "标题应匹配");
        assertEquals("月份", lineChartData.getXAxisLabel(), "X轴标签应为'月份'");
        assertEquals("支出金额", lineChartData.getYAxisLabel(), "Y轴标签应为'支出金额'");

        // 验证数据系列
        Map<String, List<ChartDataPoint>> series = lineChartData.getSeries();
        assertNotNull(series, "数据系列不应为null");
        assertTrue(series.containsKey("月度支出"), "应包含'月度支出'系列");

        List<ChartDataPoint> monthlyPoints = series.get("月度支出");
        assertNotNull(monthlyPoints, "月度支出数据点不应为null");
        assertEquals(12, monthlyPoints.size(), "应有12个月的数据点");

        // 验证特定月份的金额
        for (ChartDataPoint point : monthlyPoints) {
            if ("01月".equals(point.getLabel())) {
                // 1月总额: 120.50 + 200.00 + 80.00 + 1500.00 + 50.00 = 1950.50
                assertEquals(1950.50, point.getValue(), 0.01, "1月总额应正确");
            }
            if ("02月".equals(point.getLabel())) {
                // 2月总额: 150.75 + 150.00 + 300.00 + 1500.00 = 2100.75
                assertEquals(2100.75, point.getValue(), 0.01, "2月总额应正确");
            }
            if ("03月".equals(point.getLabel())) {
                // 3月总额: 180.25 + 500.00 + 60.00 + 1500.00 = 2240.25
                assertEquals(2240.25, point.getValue(), 0.01, "3月总额应正确");
            }
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {2024, 2025, 2026})
    @DisplayName("测试不同年份的月度支出折线图")
    void testGenerateMonthlyExpenseLineChartDifferentYears(int year) {
        // 执行方法
        TimeSeriesData lineChartData = chartService.generateMonthlyExpenseLineChart(testTransactions, year, "年度测试");

        // 验证结果
        assertNotNull(lineChartData, "折线图数据不应为null");
        assertEquals("年度测试", lineChartData.getTitle(), "标题应匹配");

        // 验证数据系列
        Map<String, List<ChartDataPoint>> series = lineChartData.getSeries();
        List<ChartDataPoint> monthlyPoints = series.get("月度支出");

        // 2024应只有12月有数据，2025应有1-3月有数据，2026应无数据
        if (year == 2024) {
            for (ChartDataPoint point : monthlyPoints) {
                if ("12月".equals(point.getLabel())) {
                    // 12月总额: 100.00 + 180.00 = 280.00
                    assertEquals(280.00, point.getValue(), 0.01, "2024年12月总额应正确");
                }
            }
        } else if (year == 2025) {
            // 已在前面的测试中验证
        } else if (year == 2026) {
            // 验证所有月份都是零值
            for (ChartDataPoint point : monthlyPoints) {
                assertEquals(0.0, point.getValue(), 0.01, "2026年所有月份应为零值");
            }
        }
    }

    @Test
    @DisplayName("测试生成分类比较柱状图")
    void testGenerateCategoryComparisonBarChart() {
        // 准备指定分类列表
        List<String> categories = Arrays.asList("食品", "交通", "娱乐", "住房");

        // 执行方法
        CategoryChartData barChartData = chartService.generateCategoryComparisonBarChart(
                testTransactions, categories, "分类比较测试");

        // 验证结果
        assertNotNull(barChartData, "柱状图数据不应为null");
        assertEquals("分类比较测试", barChartData.getTitle(), "标题应匹配");
        assertEquals("支出类别", barChartData.getXAxisLabel(), "X轴标签应为'支出类别'");
        assertEquals("金额", barChartData.getYAxisLabel(), "Y轴标签应为'金额'");

        // 验证分类数据
        assertEquals(categories, barChartData.getCategories(), "分类列表应匹配");

        // 验证系列数据
        Map<String, List<Double>> seriesData = barChartData.getSeries();
        assertNotNull(seriesData, "系列数据不应为null");
        assertTrue(seriesData.containsKey("支出金额"), "应包含'支出金额'系列");

        List<Double> values = seriesData.get("支出金额");
        assertNotNull(values, "值列表不应为null");
        assertEquals(4, values.size(), "应有4个分类的值");

        // 验证每个分类的总额
        assertEquals(551.50, values.get(0), 0.01, "食品类别总额应正确");
        assertEquals(1030.00, values.get(1), 0.01, "交通类别总额应正确");
        assertEquals(440.00, values.get(2), 0.01, "娱乐类别总额应正确");
        assertEquals(4500.00, values.get(3), 0.01, "住房类别总额应正确");
    }

    @Test
    @DisplayName("测试不提供分类列表的柱状图生成")
    void testGenerateCategoryBarChartWithoutCategories() {
        // 执行方法 - 不指定分类列表
        CategoryChartData barChartData = chartService.generateCategoryComparisonBarChart(
                testTransactions, null, "自动分类测试");

        // 验证结果
        assertNotNull(barChartData, "柱状图数据不应为null");
        List<String> extractedCategories = barChartData.getCategories();
        assertNotNull(extractedCategories, "分类列表不应为null");

        // 验证是否提取了所有不同的分类
        assertTrue(extractedCategories.contains("食品"), "应包含食品分类");
        assertTrue(extractedCategories.contains("交通"), "应包含交通分类");
        assertTrue(extractedCategories.contains("娱乐"), "应包含娱乐分类");
        assertTrue(extractedCategories.contains("住房"), "应包含住房分类");
    }

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
        assertNotNull(chartData, "图表数据不应为null");
        assertEquals(period, chartData.getPeriod(), "周期应匹配");

        // 验证数据系列
        Map<String, List<ChartDataPoint>> series = chartData.getSeries();
        assertNotNull(series, "数据系列不应为null");
        assertTrue(series.containsKey("支出趋势"), "应包含'支出趋势'系列");

        // 验证至少有一个数据点
        List<ChartDataPoint> dataPoints = series.get("支出趋势");
        assertFalse(dataPoints.isEmpty(), "数据点列表不应为空");

        // 根据不同周期验证数据点数量和格式
        switch (period) {
            case DAILY:
                // 不需要精确验证每天，但应确保有正确格式的数据点
                for (ChartDataPoint point : dataPoints) {
                    assertTrue(point.getLabel().matches("\\d{2}-\\d{2}"),
                            "每日数据点标签格式应为MM-DD");
                }
                break;
            case WEEKLY:
                // 验证周数据点格式
                for (ChartDataPoint point : dataPoints) {
                    assertTrue(point.getLabel().matches("\\d{4}-W\\d{1,2}"),
                            "每周数据点标签格式应为YYYY-WW");
                }
                break;
            case MONTHLY:
                // 验证月数据点
                assertEquals(3, dataPoints.size(), "应有3个月的数据点");
                for (ChartDataPoint point : dataPoints) {
                    assertTrue(point.getLabel().matches("\\d{4}-\\d{2}"),
                            "每月数据点标签格式应为YYYY-MM");
                }
                break;
            case YEARLY:
                // 验证年数据点 - 只应有2025年
                assertEquals(1, dataPoints.size(), "应只有1个年份的数据点");
                assertEquals("2025", dataPoints.get(0).getLabel(), "年份标签应为2025");
                // 2025年1-3月总额: 1950.50 + 2100.75 + 2240.25 = 6291.50
                assertEquals(6291.50, dataPoints.get(0).getValue(), 0.01, "2025年总额应正确");
                break;
        }
    }

    @Test
    @DisplayName("测试多类别时间序列图")
    void testGenerateMultiCategoryTimeSeriesChart() {
        // 指定特定分类
        List<String> categories = Arrays.asList("食品", "交通", "住房");

        // 执行方法
        TimeSeriesData chartData = chartService.generateMultiCategoryTimeSeriesChart(
                testTransactions, 2025, categories);

        // 验证结果
        assertNotNull(chartData, "图表数据不应为null");
        assertEquals("2025年各类别月度支出", chartData.getTitle(), "标题应正确");

        // 验证数据系列
        Map<String, List<ChartDataPoint>> series = chartData.getSeries();
        assertNotNull(series, "数据系列不应为null");
        assertEquals(3, series.size(), "应有3个数据系列");

        // 验证每个分类的系列存在
        assertTrue(series.containsKey("食品"), "应包含食品系列");
        assertTrue(series.containsKey("交通"), "应包含交通系列");
        assertTrue(series.containsKey("住房"), "应包含住房系列");

        // 验证每个系列的数据点
        for (String category : categories) {
            List<ChartDataPoint> points = series.get(category);
            assertNotNull(points, category + "系列的数据点不应为null");
            assertEquals(12, points.size(), category + "系列应有12个月的数据点");
        }

        // 验证特定月份和分类的值
        for (ChartDataPoint point : series.get("食品")) {
            if ("01月".equals(point.getLabel())) {
                assertEquals(120.50, point.getValue(), 0.01, "1月食品支出应正确");
            } else if ("02月".equals(point.getLabel())) {
                assertEquals(150.75, point.getValue(), 0.01, "2月食品支出应正确");
            } else if ("03月".equals(point.getLabel())) {
                assertEquals(180.25, point.getValue(), 0.01, "3月食品支出应正确");
            }
        }
    }

    @Test
    @DisplayName("测试不指定类别的多类别时间序列图")
    void testGenerateMultiCategoryChartWithoutCategories() {
        // 执行方法 - 不指定分类列表
        TimeSeriesData chartData = chartService.generateMultiCategoryTimeSeriesChart(
                testTransactions, 2025, null);

        // 验证结果
        assertNotNull(chartData, "图表数据不应为null");

        // 验证数据系列 - 应自动提取所有分类
        Map<String, List<ChartDataPoint>> series = chartData.getSeries();
        assertNotNull(series, "数据系列不应为null");

        // 验证是否提取了所有不同的分类
        assertTrue(series.containsKey("食品"), "应包含食品系列");
        assertTrue(series.containsKey("交通"), "应包含交通系列");
        assertTrue(series.containsKey("娱乐"), "应包含娱乐系列");
        assertTrue(series.containsKey("住房"), "应包含住房系列");
    }
}
