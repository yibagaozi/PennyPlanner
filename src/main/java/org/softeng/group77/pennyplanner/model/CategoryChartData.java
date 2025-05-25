package org.softeng.group77.pennyplanner.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Model class containing data for rendering category-based charts.
 * Provides configuration and data series for visualization components
 * in the PennyPlanner application.
 *
 * @author XI Yu
 * @version 2.0.0
 * @since 1.1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryChartData {
    private String title;
    private String xAxisLabel;
    private String yAxisLabel;
    private List<String> categories;
    private Map<String, List<Double>> series;
    private Map<String, String> seriesColors;
    private boolean showLegend;
    private boolean animated;
}
