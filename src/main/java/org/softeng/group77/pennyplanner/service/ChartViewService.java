package org.softeng.group77.pennyplanner.service;

import org.softeng.group77.pennyplanner.model.CategoryChartData;
import org.softeng.group77.pennyplanner.model.PieChartData;
import org.softeng.group77.pennyplanner.model.TimeSeriesData;

import javafx.scene.chart.*;
import org.springframework.stereotype.Component;

/**
 * Provides services for creating JavaFX chart components from data models.
 * Converts chart data objects into visual JavaFX chart representations.
 *
 * @author CHAI Yihang
 * @version 2.0.0
 * @since 1.1.0
 */
@Component
public interface ChartViewService {

    /**
     * Creates a JavaFX PieChart from the provided data model
     *
     * @param pieChartData the data model containing pie chart values
     * @return configured JavaFX PieChart component
     */
    PieChart createPieChart(PieChartData pieChartData);

    /**
     * Creates a JavaFX LineChart from the provided time series data
     *
     * @param timeSeriesData the data model containing time series values
     * @return configured JavaFX LineChart component
     */
    LineChart<String, Number> createLineChart(TimeSeriesData timeSeriesData);

    /**
     * Creates a JavaFX BarChart from the provided time series data
     *
     * @param timeSeriesData the data model containing time series values
     * @return configured JavaFX BarChart component
     */
    BarChart<String, Number> createBarChart(TimeSeriesData timeSeriesData);

    /**
     * Creates a JavaFX BarChart from category comparison data
     *
     * @param categoryChartData the data model containing category comparison values
     * @return configured JavaFX BarChart component
     */
    BarChart<String, Number> createCategoryBarChart(CategoryChartData categoryChartData);

    /**
     * Creates a JavaFX StackedBarChart from the provided time series data
     *
     * @param timeSeriesData the data model containing multiple series of time-based values
     * @return configured JavaFX StackedBarChart component
     */
    StackedBarChart<String, Number> createStackedBarChart(TimeSeriesData timeSeriesData);

}
