package org.softeng.group77.pennyplanner.model;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor  // 自动生成全参构造函数
@NoArgsConstructor   // 自动生成无参构造函数
public class Budget {

    private double amount;  // 预算金额
    private LocalDate date; // 预算日期
}
