package org.softeng.group77.pennyplanner.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
