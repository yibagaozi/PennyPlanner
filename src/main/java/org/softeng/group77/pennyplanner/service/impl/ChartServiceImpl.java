package org.softeng.group77.pennyplanner.service.impl;

import org.softeng.group77.pennyplanner.model.*;
import org.softeng.group77.pennyplanner.service.ChartService;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChartServiceImpl implements ChartService {

    private static final List<String> CHART_COLORS = Arrays.asList(
        "#4285F4", "#EA4335", "#FBBC05", "#34A853", "#FF6D01",
        "#46BDC6", "#7BAAF7", "#F6AEA9", "#FDE293", "#AEDCB7"
    );

    /**
     * Generates data for a pie chart showing the distribution of expenses by category for the current month.
     * Filters the provided list of transactions to include only expenses from the current month,
     * groups them by category, calculates the total expense for each category, and prepares data points
     * with labels, absolute values, and assigned colors for the pie chart.
     *
     * @param transactions A list of Transaction objects to analyze.
     * @param title The title for the pie chart. If null, a default title "Expense Distribution" is used.
     * @return A PieChartData object containing the data points for the category distribution pie chart.
     */
    @Override
    public PieChartData generateCategoryDistributionPieChart(List<Transaction> transactions, String title) {
        // 获取当前月份的起始和结束时间
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);

        Map<String, Double> categoryTotals = transactions.stream()
            .filter(tx -> tx.getCategory() != null && !tx.getCategory().isEmpty())

                // 筛选条件2: 只包含当前月份的交易
                .filter(tx -> !tx.getTransactionDateTime().isBefore(startOfMonth) && tx.getTransactionDateTime().isBefore(endOfMonth))
                // 筛选条件3: 只包含支出（金额小于0）
                .filter(tx -> tx.getAmountAsDouble() < 0)

            .collect(Collectors.groupingBy(
                Transaction::getCategory,
                Collectors.summingDouble(Transaction::getAmountAsDouble)
            ));

        List<ChartDataPoint> dataPoints = new ArrayList<>();
        int colorIndex = 0;

        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {

            String color = CHART_COLORS.get(colorIndex % CHART_COLORS.size());
            colorIndex++;

            dataPoints.add(ChartDataPoint.builder()
                .label(entry.getKey())
                .value(Math.abs(entry.getValue()))
                .color(color)
                .build());
        }

        dataPoints.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        return PieChartData.builder()
            .title(title != null ? title : "Expense Distribution")
            .dataPoints(dataPoints)
            .showLegend(true)
            .showLabels(true)
            .animated(true)
            .build();
    }

    /**
     * Generates data for a time series line chart showing the monthly expense trend for a specific year.
     * Filters the provided list of transactions to include only expenses from the given year,
     * groups them by month, calculates the total expense for each month, and prepares data points
     * for the line chart.
     *
     * @param transactions A list of Transaction objects to analyze.
     * @param year The year for which to generate the monthly expense chart.
     * @param title The title for the time series line chart. If null, a default title based on the year is used.
     * @return A TimeSeriesData object containing the data points for the monthly expense trend line chart.
     */
    @Override
    public TimeSeriesData generateMonthlyExpenseLineChart(List<Transaction> transactions, int year, String title) {
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MM月");

        Map<String, List<Transaction>> monthlyTransactions = transactions.stream()
            .filter(tx -> tx.getTransactionDateTime().getYear() == year)

                // 重要筛选：只包含支出（金额小于0）
                .filter(tx -> tx.getAmountAsDouble() < 0)

                .collect(Collectors.groupingBy(tx ->
                monthFormatter.format(tx.getTransactionDateTime())
            ));

        List<ChartDataPoint> monthlyDataPoints = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            String monthLabel = String.format("%02d月", month);

            double totalAmount = monthlyTransactions.getOrDefault(monthLabel, Collections.emptyList())
                .stream()
                .mapToDouble(Transaction::getAmountAsDouble)
                .sum();

            monthlyDataPoints.add(ChartDataPoint.builder()
                .label(monthLabel)
                .value(Math.abs(totalAmount))
                .seriesName("Monthly Expense")
                .color("#4285F4")
                .build());
        }

        Map<String, List<ChartDataPoint>> series = new HashMap<>();
        series.put("Monthly Expense", monthlyDataPoints);

        return TimeSeriesData.builder()
            .title(title != null ? title : year + "Monthly Expense Trend")
            .xAxisLabel("Month")
            .yAxisLabel("Amount")
            .series(series)
            .showLegend(true)
            .animated(true)
            .period(TimeSeriesData.ChartPeriod.MONTHLY)
            .build();
    }

    /**
     * Generates data for a category comparison bar chart based on the provided transactions and categories.
     * If a list of categories is not provided, it extracts distinct categories from the transactions.
     * Groups transactions by category, calculates the total amount for each category, and prepares data points
     * for the bar chart using the absolute values of the totals.
     *
     * @param transactions A list of Transaction objects to analyze.
     * @param categories An optional list of categories to include in the chart. If null or empty, distinct categories from transactions will be used.
     * @param title The title for the category comparison bar chart. If null, a default title "Category Comparison" is used.
     * @return A CategoryChartData object containing the data points for the category comparison bar chart.
     */
    @Override
    public CategoryChartData generateCategoryComparisonBarChart(List<Transaction> transactions, List<String> categories, String title) {
        if (categories == null || categories.isEmpty()) {
            categories = transactions.stream()
                .map(Transaction::getCategory)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        }

        Map<String, Double> categoryTotals = transactions.stream()
            .filter(tx -> tx.getCategory() != null)
            .collect(Collectors.groupingBy(
                Transaction::getCategory,
                Collectors.summingDouble(Transaction::getAmountAsDouble)
            ));

        Map<String, List<Double>> seriesData = new HashMap<>();
        List<Double> values = new ArrayList<>();

        for (String category : categories) {
            double amount = Math.abs(categoryTotals.getOrDefault(category, 0.0));
            values.add(amount);
        }

        seriesData.put("Amount", values);

        Map<String, String> seriesColors = new HashMap<>();
        seriesColors.put("Amount", "#4285F4");

        return CategoryChartData.builder()
            .title(title != null ? title : "Category Comparison")
            .xAxisLabel("Category")
            .yAxisLabel("Amount")
            .categories(categories)
            .series(seriesData)
            .seriesColors(seriesColors)
            .showLegend(true)
            .animated(true)
            .build();
    }

    /**
     * Generates time series data for charting expense trends within a specified date range and period.
     * Filters transactions by the given start and end dates, then groups and sums expenses
     * according to the specified chart period (DAILY, WEEKLY, MONTHLY, YEARLY).
     *
     * @param transactions A list of Transaction objects to analyze.
     * @param startDate The start date (inclusive) of the date range.
     * @param endDate The end date (inclusive) of the date range.
     * @param period The time period for grouping the data (DAILY, WEEKLY, MONTHLY, YEARLY).
     * @return A TimeSeriesData object containing the data points for the expense trend chart.
     */
    @Override
    public TimeSeriesData generateDateRangeExpenseChart(List<Transaction> transactions, LocalDate startDate, LocalDate endDate, TimeSeriesData.ChartPeriod period) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        List<Transaction> filteredTransactions = transactions.stream()
            .filter(tx -> !tx.getTransactionDateTime().isBefore(startDateTime) &&
                         tx.getTransactionDateTime().isBefore(endDateTime))
            .collect(Collectors.toList());

        DateTimeFormatter formatter;
        Map<String, List<ChartDataPoint>> seriesMap = new HashMap<>();
        List<ChartDataPoint> dataPoints = new ArrayList<>();

        switch (period) {
            case DAILY:
                formatter = DateTimeFormatter.ofPattern("MM-dd");
                groupByDateFormat(filteredTransactions, formatter, dataPoints);
                break;
            case WEEKLY:
                groupByWeek(filteredTransactions, dataPoints);
                break;
            case MONTHLY:
                formatter = DateTimeFormatter.ofPattern("yyyy-MM");
                groupByDateFormat(filteredTransactions, formatter, dataPoints);
                break;
            case YEARLY:
                formatter = DateTimeFormatter.ofPattern("yyyy");
                groupByDateFormat(filteredTransactions, formatter, dataPoints);
                break;
            default:
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                groupByDateFormat(filteredTransactions, formatter, dataPoints);
        }

        seriesMap.put("Expense Trend", dataPoints);

        return TimeSeriesData.builder()
            .title(startDate + " to " + endDate + " Expense Trend")
            .xAxisLabel("Date")
            .yAxisLabel("Amount")
            .series(seriesMap)
            .showLegend(true)
            .animated(true)
            .period(period)
            .build();
    }

    /**
     * Generates time series data for charting monthly expense trends across multiple categories for a specific year.
     * Filters transactions by the given year and groups them by category and month to calculate monthly totals for each category.
     * Prepares data points for a multi-series line chart, where each series represents a category.
     * If a list of categories is not provided, it extracts distinct categories from the transactions.
     *
     * @param transactions A list of Transaction objects to analyze.
     * @param year The year for which to generate the monthly expense trend chart by category.
     * @param categories An optional list of categories to include in the chart. If null or empty, distinct categories from transactions will be used.
     * @return A TimeSeriesData object containing the data points and series for the multi-category monthly expense trend chart.
     */
    @Override
    public TimeSeriesData generateMultiCategoryTimeSeriesChart(List<Transaction> transactions, int year, List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            categories = transactions.stream()
                .map(Transaction::getCategory)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        }

        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MM月");

        List<Transaction> yearTransactions = transactions.stream()
            .filter(tx -> tx.getTransactionDateTime().getYear() == year)
            .collect(Collectors.toList());

        Map<String, List<ChartDataPoint>> seriesMap = new HashMap<>();

        int colorIndex = 0;
        for (String category : categories) {
            String color = CHART_COLORS.get(colorIndex % CHART_COLORS.size());
            colorIndex++;

            List<ChartDataPoint> categoryPoints = new ArrayList<>();

            for (int month = 1; month <= 12; month++) {
                final int currentMonth = month;
                String monthLabel = String.format("%02d month", month);

                double monthCategoryTotal = yearTransactions.stream()
                    .filter(tx -> tx.getTransactionDateTime().getMonthValue() == currentMonth &&
                                category.equals(tx.getCategory()))
                    .mapToDouble(Transaction::getAmountAsDouble)
                    .sum();

                categoryPoints.add(ChartDataPoint.builder()
                    .label(monthLabel)
                    .value(Math.abs(monthCategoryTotal))
                    .seriesName(category)
                    .color(color)
                    .build());
            }

            seriesMap.put(category, categoryPoints);
        }

        return TimeSeriesData.builder()
            .title(year + "yearly expense trend by category")
            .xAxisLabel("Month")
            .yAxisLabel("Amount")
            .series(seriesMap)
            .showLegend(true)
            .animated(true)
            .period(TimeSeriesData.ChartPeriod.MONTHLY)
            .build();
    }

    private void groupByDateFormat(List<Transaction> transactions, DateTimeFormatter formatter, List<ChartDataPoint> dataPoints) {
        Map<String, Double> dateTotals = transactions.stream()
            .collect(Collectors.groupingBy(
                tx -> formatter.format(tx.getTransactionDateTime()),
                Collectors.summingDouble(Transaction::getAmountAsDouble)
            ));

        List<String> sortedDates = new ArrayList<>(dateTotals.keySet());
        Collections.sort(sortedDates);

        for (String date : sortedDates) {
            dataPoints.add(ChartDataPoint.builder()
                .label(date)
                .value(Math.abs(dateTotals.get(date)))
                .seriesName("Expense Trend")
                .color("#4285F4")
                .build());
        }
    }

    private void groupByWeek(List<Transaction> transactions, List<ChartDataPoint> dataPoints) {
        Map<String, Double> weekTotals = new HashMap<>();

        for (Transaction tx : transactions) {
            LocalDate date = tx.getTransactionDateTime().toLocalDate();
            int weekOfYear = date.get(java.time.temporal.WeekFields.of(Locale.getDefault()).weekOfYear());
            String key = date.getYear() + "-W" + weekOfYear;

            weekTotals.put(key, weekTotals.getOrDefault(key, 0.0) + tx.getAmountAsDouble());
        }

        List<String> sortedWeeks = new ArrayList<>(weekTotals.keySet());
        Collections.sort(sortedWeeks);

        for (String week : sortedWeeks) {
            dataPoints.add(ChartDataPoint.builder()
                .label(week)
                .value(Math.abs(weekTotals.get(week)))
                .seriesName("Expense Trend")
                .color("#4285F4")
                .build());
        }
    }

}
