package org.softeng.group77.pennyplanner.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model class representing a data point for charts and visualizations.
 * Used to store information needed to render graphical data representations
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
public class ChartDataPoint {
    private String label;       // X轴标签或分类名称
    private double value;       // Y轴值或数量
    private String seriesName;  // 数据系列名称
    private String color;       // 显示颜色
}
