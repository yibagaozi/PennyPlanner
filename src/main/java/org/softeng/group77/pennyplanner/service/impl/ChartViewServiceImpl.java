package org.softeng.group77.pennyplanner.service.impl;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.*;
import javafx.scene.control.Tooltip;
import lombok.extern.slf4j.Slf4j;
import org.softeng.group77.pennyplanner.model.CategoryChartData;
import org.softeng.group77.pennyplanner.model.ChartDataPoint;
import org.softeng.group77.pennyplanner.model.PieChartData;
import org.softeng.group77.pennyplanner.model.TimeSeriesData;
import org.softeng.group77.pennyplanner.service.ChartViewService;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Slf4j
public class ChartViewServiceImpl implements ChartViewService {

    /**
     * Creates a JavaFX PieChart instance based on the provided PieChartData.
     * Converts data points from the PieChartData to JavaFX PieChart.Data, sets chart properties
     * like title, labels visibility, legend visibility, and animation. It also applies
     * custom colors and adds tooltips to each pie slice based on the data points.
     *
     * @param pieChartData The PieChartData object containing the data and configuration for the pie chart.
     * @return A configured JavaFX PieChart object ready to be displayed.
     */
    @Override
    public PieChart createPieChart(PieChartData pieChartData) {

        ObservableList<PieChart.Data> pieChartObsData = FXCollections.observableArrayList();

        for (ChartDataPoint point : pieChartData.getDataPoints()) {
            pieChartObsData.add(new PieChart.Data(point.getLabel(), point.getValue()));
        }

        PieChart pieChart = new PieChart(pieChartObsData);
        pieChart.setTitle(pieChartData.getTitle());
        pieChart.setLabelsVisible(pieChartData.isShowLabels());
        pieChart.setLegendVisible(pieChartData.isShowLegend());
        pieChart.setAnimated(pieChartData.isAnimated());

        int i = 0;
        for (PieChart.Data data : pieChart.getData()) {
            if (i < pieChartData.getDataPoints().size()) {
                String color = pieChartData.getDataPoints().get(i).getColor();
                data.getNode().setStyle("-fx-pie-color: " + color + ";");

                Tooltip tooltip = new Tooltip(
                    String.format("%s: ¥%.2f", data.getName(), data.getPieValue())
                );
                Tooltip.install(data.getNode(), tooltip);

                i++;
            }
        }

        return pieChart;
    }

    /**
     * Creates a JavaFX LineChart instance based on the provided TimeSeriesData.
     * Configures the chart's axes, title, animation, and legend. Adds data series
     * from the TimeSeriesData to the chart, where each series represents a trend over time.
     *
     * @param timeSeriesData The TimeSeriesData object containing the data and configuration for the time series chart.
     * @return A configured JavaFX LineChart object ready to be displayed.
     */
    @Override
    public LineChart<String, Number> createLineChart(TimeSeriesData timeSeriesData) {

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(timeSeriesData.getXAxisLabel());
        yAxis.setLabel(timeSeriesData.getYAxisLabel());

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle(timeSeriesData.getTitle());
        lineChart.setAnimated(timeSeriesData.isAnimated());
        lineChart.setLegendVisible(timeSeriesData.isShowLegend());

        for (Map.Entry<String, List<ChartDataPoint>> entry : timeSeriesData.getSeries().entrySet()) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(entry.getKey());

            for (ChartDataPoint point : entry.getValue()) {
                series.getData().add(new XYChart.Data<>(point.getLabel(), point.getValue()));
            }

            lineChart.getData().add(series);
        }

        styleLineChartSeries(lineChart, timeSeriesData);

        return lineChart;
    }

    /**
     * Creates a JavaFX BarChart instance based on the provided TimeSeriesData.
     * Configures the chart's axes, title, animation, legend, and bar/category gaps.
     * Adds data series from the TimeSeriesData to the chart, where each series represents a set of bars over time or categories.
     *
     * @param timeSeriesData The TimeSeriesData object containing the data and configuration for the bar chart.
     * @return A configured JavaFX BarChart object ready to be displayed.
     */
    @Override
    public BarChart<String, Number> createBarChart(TimeSeriesData timeSeriesData) {

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(timeSeriesData.getXAxisLabel());
        yAxis.setLabel(timeSeriesData.getYAxisLabel());

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle(timeSeriesData.getTitle());
        barChart.setAnimated(timeSeriesData.isAnimated());
        barChart.setLegendVisible(timeSeriesData.isShowLegend());

        barChart.setBarGap(3);
        barChart.setCategoryGap(20);

        for (Map.Entry<String, List<ChartDataPoint>> entry : timeSeriesData.getSeries().entrySet()) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(entry.getKey());

            for (ChartDataPoint point : entry.getValue()) {
                series.getData().add(new XYChart.Data<>(point.getLabel(), point.getValue()));
            }

            barChart.getData().add(series);
        }

        styleBarChartSeries(barChart, timeSeriesData);

        return barChart;
    }

    /**
     * Creates a JavaFX BarChart instance based on the provided CategoryChartData.
     * Configures the chart's axes, title, animation, legend, and bar/category gaps.
     * Sets the categories for the X-axis. Adds data series from the CategoryChartData,
     * where each series represents a set of bars for different categories. Applies custom
     * colors to the bars and adds tooltips to each bar.
     *
     * @param categoryChartData The CategoryChartData object containing the data and configuration for the category bar chart.
     * @return A configured JavaFX BarChart object ready to be displayed.
     */
    @Override
    public BarChart<String, Number> createCategoryBarChart(CategoryChartData categoryChartData) {

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(categoryChartData.getXAxisLabel());
        yAxis.setLabel(categoryChartData.getYAxisLabel());
        xAxis.setCategories(FXCollections.observableArrayList(categoryChartData.getCategories()));

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle(categoryChartData.getTitle());
        barChart.setAnimated(categoryChartData.isAnimated());
        barChart.setLegendVisible(categoryChartData.isShowLegend());

        barChart.setBarGap(3);
        barChart.setCategoryGap(20);

        for (Map.Entry<String, List<Double>> entry : categoryChartData.getSeries().entrySet()) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(entry.getKey());

            List<Double> values = entry.getValue();
            for (int i = 0; i < categoryChartData.getCategories().size() && i < values.size(); i++) {
                series.getData().add(new XYChart.Data<>(
                    categoryChartData.getCategories().get(i),
                    values.get(i)
                ));
            }

            barChart.getData().add(series);
        }

        if (categoryChartData.getSeriesColors() != null) {
            for (int i = 0; i < barChart.getData().size(); i++) {
                XYChart.Series<String, Number> series = barChart.getData().get(i);
                String color = categoryChartData.getSeriesColors().get(series.getName());

                if (color != null) {
                    for (XYChart.Data<String, Number> data : series.getData()) {
                        data.getNode().setStyle("-fx-bar-fill: " + color + ";");
                    }
                }
            }
        }

        for (XYChart.Series<String, Number> series : barChart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                Tooltip tooltip = new Tooltip(
                    String.format("%s\n%s: ¥%.2f", series.getName(), data.getXValue(), data.getYValue().doubleValue())
                );
                Tooltip.install(data.getNode(), tooltip);
            }
        }

        return barChart;
    }

    /**
     * Creates a JavaFX StackedBarChart instance based on the provided TimeSeriesData.
     * Configures the chart's axes, title, animation, and legend. Adds data series
     * from the TimeSeriesData to the chart, where each series represents a stack of bars over time or categories.
     * Applies colors to the bars based on the series configuration and adds tooltips to each bar displaying series name, category, and formatted value.
     *
     * @param timeSeriesData The TimeSeriesData object containing the data and configuration for the stacked bar chart.
     * @return A configured JavaFX StackedBarChart object ready to be displayed.
     */
    @Override
    public StackedBarChart<String, Number> createStackedBarChart(TimeSeriesData timeSeriesData) {

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(timeSeriesData.getXAxisLabel());
        yAxis.setLabel(timeSeriesData.getYAxisLabel());

        StackedBarChart<String, Number> stackedBarChart = new StackedBarChart<>(xAxis, yAxis);
        stackedBarChart.setTitle(timeSeriesData.getTitle());
        stackedBarChart.setAnimated(timeSeriesData.isAnimated());
        stackedBarChart.setLegendVisible(timeSeriesData.isShowLegend());

        for (Map.Entry<String, List<ChartDataPoint>> entry : timeSeriesData.getSeries().entrySet()) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(entry.getKey());

            for (ChartDataPoint point : entry.getValue()) {
                series.getData().add(new XYChart.Data<>(point.getLabel(), point.getValue()));
            }

            stackedBarChart.getData().add(series);
        }

        for (int i = 0; i < stackedBarChart.getData().size(); i++) {
            XYChart.Series<String, Number> series = stackedBarChart.getData().get(i);
            String seriesName = series.getName();
            String color = null;

            for (List<ChartDataPoint> points : timeSeriesData.getSeries().values()) {
                if (!points.isEmpty() && seriesName.equals(points.getFirst().getSeriesName())) {
                    color = points.getFirst().getColor();
                    break;
                }
            }

            if (color != null) {
                final String seriesColor = color;
                for (XYChart.Data<String, Number> data : series.getData()) {
                    if (data.getNode() != null) {
                        data.getNode().setStyle("-fx-bar-fill: " + seriesColor + ";");
                    }
                }
            }

            for (XYChart.Data<String, Number> data : series.getData()) {
                NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.CHINA);
                Tooltip tooltip = new Tooltip(
                    String.format("%s\n%s: %s", series.getName(), data.getXValue(),
                                 formatter.format(data.getYValue()))
                );
                Tooltip.install(data.getNode(), tooltip);
            }
        }

        return stackedBarChart;
    }

    private void styleLineChartSeries(LineChart<String, Number> lineChart, TimeSeriesData timeSeriesData) {

        for (int i = 0; i < lineChart.getData().size(); i++) {
            XYChart.Series<String, Number> series = lineChart.getData().get(i);
            String seriesName = series.getName();

            List<ChartDataPoint> points = timeSeriesData.getSeries().get(seriesName);
            if (points != null && !points.isEmpty()) {
                String color = points.getFirst().getColor();

                for (int j = 0; j < series.getData().size(); j++) {
                    XYChart.Data<String, Number> data = series.getData().get(j);

                    if (data.getNode() != null) {

                        data.getNode().setStyle(
                            "-fx-background-color: " + color + ", white;\n" +
                            "-fx-background-insets: 0, 2;\n" +
                            "-fx-background-radius: 5px;\n" +
                            "-fx-padding: 5px;"
                        );

                        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.CHINA);
                        Tooltip tooltip = new Tooltip(
                            String.format("%s\n%s: %s", seriesName, data.getXValue(),
                                         formatter.format(data.getYValue()))
                        );
                        Tooltip.install(data.getNode(), tooltip);
                    }
                }

                series.getNode().lookup(".chart-series-line").setStyle(
                    "-fx-stroke: " + color + ";\n" +
                    "-fx-stroke-width: 3px;"
                );
            }
        }
    }

    private void styleBarChartSeries(BarChart<String, Number> barChart, TimeSeriesData timeSeriesData) {

        for (int i = 0; i < barChart.getData().size(); i++) {
            XYChart.Series<String, Number> series = barChart.getData().get(i);
            String seriesName = series.getName();

            List<ChartDataPoint> points = timeSeriesData.getSeries().get(seriesName);
            if (points != null && !points.isEmpty()) {
                String color = points.getFirst().getColor();

                for (XYChart.Data<String, Number> data : series.getData()) {
                    if (data.getNode() != null) {
                        data.getNode().setStyle("-fx-bar-fill: " + color + ";");

                        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.CHINA);
                        Tooltip tooltip = new Tooltip(
                            String.format("%s\n%s: %s", seriesName, data.getXValue(),
                                         formatter.format(data.getYValue()))
                        );
                        Tooltip.install(data.getNode(), tooltip);
                    }
                }
            }
        }
    }
}
