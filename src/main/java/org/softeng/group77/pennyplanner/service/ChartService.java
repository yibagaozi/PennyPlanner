package org.softeng.group77.pennyplanner.service;

import org.softeng.group77.pennyplanner.model.CategoryChartData;
import org.softeng.group77.pennyplanner.model.PieChartData;
import org.softeng.group77.pennyplanner.model.TimeSeriesData;
import org.softeng.group77.pennyplanner.model.Transaction;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Provides chart generation services for transaction data visualization.
 * Supports various chart types for financial analysis.
 *
 * @author XI Yu
 * @version 2.0.0
 * @since 1.1.0
 */
@Component
public interface ChartService {

    /**
     * Generates a pie chart showing transaction distribution by category
     *
     * @param transactions the transactions to analyze
     * @param title the chart title
     * @return pie chart data model
     */
    PieChartData generateCategoryDistributionPieChart(List<Transaction> transactions, String title);

    /**
     * Generates a line chart of monthly expenses for a year
     *
     * @param transactions the transactions to analyze
     * @param year the year to display data for
     * @param title the chart title
     * @return time series chart data model
     */
    TimeSeriesData generateMonthlyExpenseLineChart(List<Transaction> transactions, int year, String title);

    /**
     * Generates a bar chart comparing expenses across categories
     *
     * @param transactions the transactions to analyze
     * @param categories the categories to include
     * @param title the chart title
     * @return category comparison chart data model
     */
    CategoryChartData generateCategoryComparisonBarChart(List<Transaction> transactions, List<String> categories, String title);

    /**
     * Generates a chart showing expenses over a custom date range
     *
     * @param transactions the transactions to analyze
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @param period the time period grouping (day, week, month)
     * @return time series chart data model
     */
    TimeSeriesData generateDateRangeExpenseChart(List<Transaction> transactions, LocalDate startDate, LocalDate endDate, TimeSeriesData.ChartPeriod period);

    /**
     * Generates a multi-series chart tracking multiple categories over time
     *
     * @param transactions the transactions to analyze
     * @param year the year to display data for
     * @param categories the categories to include as separate series
     * @return time series chart data model with multiple series
     */
    TimeSeriesData generateMultiCategoryTimeSeriesChart(List<Transaction> transactions, int year, List<String> categories);
}
