package org.softeng.group77.pennyplanner.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
