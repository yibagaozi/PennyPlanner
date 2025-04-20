package org.softeng.group77.pennyplanner.repository;

import org.softeng.group77.pennyplanner.model.Budget;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BudgetRepository {

    // 保存预算
    Budget save(Budget budget) throws IOException;

    // 根据日期删除预算
    boolean deleteByDate(LocalDate date) throws IOException;

    // 根据日期查找预算
    Optional<Budget> findByDate(LocalDate date) throws IOException;

    // 获取所有预算
    List<Budget> findAll() throws IOException;

    // 更新预算
    Optional<Budget> update(LocalDate date, Budget updatedBudget) throws IOException;

    // 检查指定日期的预算是否存在
    boolean exists(LocalDate date) throws IOException;

    // 获取预算数量
    long count() throws IOException;

    // 清空所有预算
    void clear() throws IOException;
}
