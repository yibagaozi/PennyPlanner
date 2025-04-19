package org.softeng.group77.pennyplanner.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

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
