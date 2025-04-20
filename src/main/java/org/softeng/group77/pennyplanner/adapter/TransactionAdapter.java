package org.softeng.group77.pennyplanner.adapter;

import org.softeng.group77.pennyplanner.controller.tableModel;
import org.softeng.group77.pennyplanner.dto.TransactionDetail;
import org.softeng.group77.pennyplanner.service.TransactionService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

@Component
public class TransactionAdapter {

    private final TransactionService transactionService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public TransactionAdapter(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * 将前端tableModel转换为后端TransactionDetail
     */
    public TransactionDetail toTransactionDetail(tableModel model) {
        TransactionDetail detail = new TransactionDetail();

        if (model.getId() != null) {
            detail.setId(model.getId());
        }

        detail.setDescription(model.getDescription());
        detail.setCategory(model.getCategory());
        detail.setAmount(BigDecimal.valueOf(model.getAmount()));

        // 转换日期字符串为LocalDateTime
        try {
            LocalDate date = LocalDate.parse(model.getDate(), DATE_FORMATTER);
            detail.setTransactionDateTime(date.atStartOfDay());
        } catch (DateTimeParseException e) {
            // 如果日期格式有问题，使用当前时间
            detail.setTransactionDateTime(LocalDateTime.now());
        }

        return detail;
    }

    /**
     * 将后端TransactionDetail转换为前端tableModel
     */
    public tableModel toTableModel(TransactionDetail detail) {
        String formattedDate = detail.getTransactionDateTime().format(DATE_FORMATTER);
        double amount = detail.getAmount().doubleValue();

        return new tableModel(
                detail.getId(),
                formattedDate,
                detail.getDescription(),
                amount,
                detail.getCategory(),
                "" // 支付方式在TransactionDetail中不存在，使用空字符串
        );
    }

    /**
     * 获取当前用户所有交易记录
     */
    public ObservableList<tableModel> getUserTransactions() {
        try {
            List<TransactionDetail> details = transactionService.getUserTransactions()
                    .stream()
                    .sorted(Comparator.comparing(TransactionDetail::getTransactionDateTime).reversed()) // 按日期降序
                    .collect(Collectors.toList());
            ObservableList<tableModel> models = FXCollections.observableArrayList();

            // 生成动态序号
            AtomicInteger index = new AtomicInteger(1);
            details.forEach(detail -> {
                tableModel model = toTableModel(detail);
                model.setDisplayId(String.valueOf(index.getAndIncrement())); // 设置递增序号
                models.add(model);
            });

            return models;
        } catch (Exception e) {
            e.printStackTrace();
            return FXCollections.observableArrayList(new ArrayList<>());
        }
    }

    /**
     * 保存新交易记录
     */
    public boolean saveTransaction(tableModel model) {
        try {
            TransactionDetail detail = toTransactionDetail(model);
            transactionService.createTransaction(detail);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 更新交易记录
     */
    public boolean updateTransaction(tableModel model) {
        try {
            TransactionDetail detail = toTransactionDetail(model);
            transactionService.updateTransaction(detail);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除交易记录
     */
    public boolean deleteTransaction(String transactionId) {
        try {
            return transactionService.deleteTransaction(transactionId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 按日期范围筛选交易
     */
    public ObservableList<tableModel> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) {
        try {
            List<TransactionDetail> details = transactionService.filterTransactionByDate(startDate, endDate);
            List<tableModel> models = details.stream()
                    .map(this::toTableModel)
                    .collect(Collectors.toList());
            return FXCollections.observableArrayList(models);
        } catch (Exception e) {
            e.printStackTrace();
            return FXCollections.observableArrayList(new ArrayList<>());
        }
    }

    /**
     * 按类别筛选交易
     */
    public ObservableList<tableModel> getTransactionsByCategory(String category) {
        try {
            List<TransactionDetail> details = transactionService.filterTransactionByCategory(category);
            List<tableModel> models = details.stream()
                    .map(this::toTableModel)
                    .collect(Collectors.toList());
            return FXCollections.observableArrayList(models);
        } catch (Exception e) {
            e.printStackTrace();
            return FXCollections.observableArrayList(new ArrayList<>());
        }
    }

    /**
     * 获取各类别的支出总和
     */
    public List<CategorySum> getCategorySummary() {
        ObservableList<tableModel> transactions = getUserTransactions();
        return transactions.stream()
                .filter(t -> t.getAmount() < 0) // 只统计支出
                .collect(Collectors.groupingBy(
                        tableModel::getCategory,
                        Collectors.summingDouble(t -> Math.abs(t.getAmount()))
                ))
                .entrySet().stream()
                .map(entry -> new CategorySum(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * 按日期获取每日支出总和
     */
    public List<DateSum> getDailyExpenseSummary(int daysCount) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(daysCount);

        ObservableList<tableModel> transactions = getTransactionsByDateRange(startDate, endDate);

        return transactions.stream()
                .filter(t -> t.getAmount() < 0) // 只统计支出
                .collect(Collectors.groupingBy(
                        tableModel::getDate,
                        Collectors.summingDouble(t -> Math.abs(t.getAmount()))
                ))
                .entrySet().stream()
                .map(entry -> new DateSum(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * 类别汇总数据结构
     */
    public static class CategorySum {
        private final String category;
        private final double amount;

        public CategorySum(String category, double amount) {
            this.category = category;
            this.amount = amount;
        }

        public String getCategory() { return category; }
        public double getAmount() { return amount; }
    }

    /**
     * 日期汇总数据结构
     */
    public static class DateSum {
        private final String date;
        private final double amount;

        public DateSum(String date, double amount) {
            this.date = date;
            this.amount = amount;
        }

        public String getDate() { return date; }
        public double getAmount() { return amount; }
    }


}
