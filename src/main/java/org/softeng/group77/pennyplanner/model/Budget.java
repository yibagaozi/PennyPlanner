package org.softeng.group77.pennyplanner.model;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * Represents a budget entity in the PennyPlanner application.
 *
 * @author JIANG Mengnan
 * @version 2.0.0
 * @since 1.1.0
 */
@Getter
@Setter
@AllArgsConstructor  // 自动生成全参构造函数
@NoArgsConstructor   // 自动生成无参构造函数
public class Budget implements Serializable {
    private static final long serialVersionUID = 1L;
    private double amount;  // 预算金额
    private LocalDate date; // 预算日期
}
