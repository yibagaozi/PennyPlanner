package org.softeng.group77.pennyplanner.repository.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.softeng.group77.pennyplanner.model.Budget;
import org.softeng.group77.pennyplanner.repository.BudgetRepository;
import org.softeng.group77.pennyplanner.repository.base.JsonDataManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Optional;

@Repository
@Slf4j
public class BudgetRepositoryImpl extends JsonDataManager<Budget> implements BudgetRepository {

    public BudgetRepositoryImpl(@Value("${app.data.path:data}/budget.json") String filePath) {
        super(filePath, new TypeReference<List<Budget>>() {}); // Corrected TypeReference
    }

    @Override
    public Budget save(Budget budget) throws IOException {
        delete(existing -> existing.getDate().equals(budget.getDate())); // Delete the same date's budget first
        return super.save(budget); // Save budget
    }

    @Override
    public boolean deleteByDate(LocalDate date) throws IOException {
        return delete(budget -> budget.getDate().equals(date)); // Delete budget by date
    }

    @Override
    public Optional<Budget> findByDate(LocalDate date) throws IOException {
        return findOne(budget -> budget.getDate().equals(date)); // Find budget by date
    }

    @Override
    public Map<LocalDate, Budget> findAll() throws IOException {
        List<Budget> budgets = loadAll();

        // 将List<Budget>转换为Map<LocalDate, Budget>
        return budgets.stream()
            .collect(Collectors.toMap(
                Budget::getDate,  // 使用日期作为键
                budget -> budget, // 值就是预算对象本身
                (existing, replacement) -> replacement // 如果有重复，保留后者
            ));
    }

    @Override
    public Optional<Budget> update(LocalDate date, Budget updatedBudget) throws IOException {
        return update(budget -> budget.getDate().equals(date), updatedBudget); // Update budget
    }

    @Override
    public boolean exists(LocalDate date) throws IOException {
        return super.exists(budget -> budget.getDate().equals(date)); // Check if budget exists for date
    }

    @Override
    public long count() throws IOException {
        return super.count(); // Get the count of budgets
    }

    @Override
    public void clear() throws IOException {
        super.clear(); // Clear all budgets
    }
}
