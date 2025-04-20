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
