package org.softeng.group77.pennyplanner.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PieChartData {
    private String title;
    private List<ChartDataPoint> dataPoints;
    private boolean showLegend;
    private boolean showLabels;
    private boolean animated;
}
