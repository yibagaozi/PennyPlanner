package org.softeng.group77.pennyplanner.service;

import org.softeng.group77.pennyplanner.model.CategoryChartData;
import org.softeng.group77.pennyplanner.model.PieChartData;
import org.softeng.group77.pennyplanner.model.TimeSeriesData;
import org.softeng.group77.pennyplanner.model.Transaction;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public interface ChartService {

    PieChartData generateCategoryDistributionPieChart(List<Transaction> transactions, String title);

    TimeSeriesData generateMonthlyExpenseLineChart(List<Transaction> transactions, int year, String title);

    CategoryChartData generateCategoryComparisonBarChart(List<Transaction> transactions, List<String> categories, String title);

    TimeSeriesData generateDateRangeExpenseChart(List<Transaction> transactions, LocalDate startDate, LocalDate endDate, TimeSeriesData.ChartPeriod period);

    TimeSeriesData generateMultiCategoryTimeSeriesChart(List<Transaction> transactions, int year, List<String> categories);
}
