package org.softeng.group77.pennyplanner.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Model class containing data for rendering time series charts.
 * Provides configuration and data points for visualizing financial data
 * over time periods in the PennyPlanner application.
 *
 * @author XI Yu
 * @version 2.0.0
 * @since 1.1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSeriesData {
    private String title;
    private String xAxisLabel;
    private String yAxisLabel;
    private Map<String, List<ChartDataPoint>> series;
    private boolean showLegend;
    private boolean animated;
    private ChartPeriod period;
    
    public enum ChartPeriod {
        DAILY, WEEKLY, MONTHLY, YEARLY
    }
}
