package org.softeng.group77.pennyplanner.service;

import org.softeng.group77.pennyplanner.model.CategoryChartData;
import org.softeng.group77.pennyplanner.model.PieChartData;
import org.softeng.group77.pennyplanner.model.TimeSeriesData;

import javafx.scene.chart.*;
import org.springframework.stereotype.Component;

@Component
public interface ChartViewService {

    PieChart createPieChart(PieChartData pieChartData);

    LineChart<String, Number> createLineChart(TimeSeriesData timeSeriesData);

    BarChart<String, Number> createBarChart(TimeSeriesData timeSeriesData);

    BarChart<String, Number> createCategoryBarChart(CategoryChartData categoryChartData);

    StackedBarChart<String, Number> createStackedBarChart(TimeSeriesData timeSeriesData);

}
