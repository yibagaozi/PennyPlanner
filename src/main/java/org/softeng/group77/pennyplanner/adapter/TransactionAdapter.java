package org.softeng.group77.pennyplanner.adapter;

import org.softeng.group77.pennyplanner.controller.tableModel;
import org.softeng.group77.pennyplanner.dto.TransactionDetail;
import org.softeng.group77.pennyplanner.dto.UserInfo;
import org.softeng.group77.pennyplanner.service.AuthService;
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

/**
 * Adapter class that serves as a bridge between the front-end table models and the back-end transaction service.
 * This class converts between the presentation layer's tableModel and the service layer's TransactionDetail objects.
 * It provides operations for managing financial transactions in the PennyPlanner application.
 *
 * @author CHAI Jiayang
 * @version 2.0.0
 */
@Component
public class TransactionAdapter {

    /**
     * Service responsible for transaction-related operations in the application.
     */
    private final TransactionService transactionService;

    /**
     * Service responsible for authentication and user management operations.
     */
    private final AuthService authService; // 添加AuthService

    /**
     * Date formatter for parsing and formatting dates in the "yyyy-MM-dd" pattern.
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Constructs a new TransactionAdapter with the specified services.
     *
     * @param transactionService the service to handle transaction operations
     * @param authService the service to handle authentication operations
     */
    public TransactionAdapter(TransactionService transactionService, AuthService authService) {
        this.transactionService = transactionService;
        this.authService = authService;
    }

    /**
     * Converts a front-end tableModel object to a back-end TransactionDetail object.
     * Sets the current user ID and handles date conversion.
     *
     * @param model the tableModel to convert
     * @return the converted TransactionDetail object
     */
    public TransactionDetail toTransactionDetail(tableModel model) {
        TransactionDetail detail = new TransactionDetail();

        if (model.getId() != null) {
            detail.setId(model.getId());
        }

        detail.setDescription(model.getDescription());
        detail.setCategory(model.getCategory());
        detail.setAmount(BigDecimal.valueOf(model.getAmount()));
        detail.setMethod(model.getMethod());

        // 设置当前用户ID - 关键修复点
        try {
            UserInfo currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                detail.setUserId(currentUser.getId());
            }
        } catch (Exception e) {
            System.out.println("获取用户ID失败: " + e.getMessage());
        }


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
     * Converts TransactionDetail object to a JavaFX tableModel object.
     *
     * @param detail the TransactionDetail to convert
     * @return the converted tableModel object
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
                detail.getMethod()
        );
    }

    /**
     * Retrieves all transactions for the current user, sorted by date in descending order.
     *
     * @return an ObservableList of tableModel objects representing the user's transactions
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
     * Saves a new transaction to the database.
     *
     * @param model the tableModel representing the transaction to save
     * @return true if the operation was successful, false otherwise
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
     * Updates an existing transaction in the database.
     *
     * @param model the tableModel representing the transaction to update
     * @return true if the operation was successful, false otherwise
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
     * Deletes a transaction from the database.
     *
     * @param transactionId the ID of the transaction to delete
     * @return true if the operation was successful, false otherwise
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
     * Retrieves transactions within the specified date range.
     *
     * @param startDate the start date of the range (inclusive)
     * @param endDate the end date of the range (inclusive)
     * @return an ObservableList of tableModel objects representing the filtered transactions
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
     * Retrieves transactions of the specified category.
     *
     * @param category the category to filter by
     * @return an ObservableList of tableModel objects representing the filtered transactions
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
     * Calculates the sum of expenses for each category.
     *
     * @return a list of CategorySum objects containing category names and their total expense amounts
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
     * Calculates the daily expense summary for the specified number of recent days.
     *
     * @param daysCount the number of days to include in the summary
     * @return a list of DateSum objects containing dates and their total expense amounts
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
     * Retrieves transactions with the specified payment method.
     *
     * @param method the payment method to filter by
     * @return an ObservableList of tableModel objects representing the filtered transactions
     */
    public ObservableList<tableModel> getTransactionsByMethod(String method) {
        try {
            List<TransactionDetail> details = transactionService.filterTransactionByMethod(method);
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
     * Data structure for representing category summary information.
     */
    public static class CategorySum {
        private final String category;
        private final double amount;

        /**
         * Constructs a new CategorySum with the specified category and amount.
         *
         * @param category the expense category
         * @param amount the total amount for the category
         */
        public CategorySum(String category, double amount) {
            this.category = category;
            this.amount = amount;
        }

        public String getCategory() { return category; }
        public double getAmount() { return amount; }
    }

    /**
     * Data structure for representing daily expense summary information.
     */
    public static class DateSum {
        private final String date;
        private final double amount;

        /**
         * Constructs a new DateSum with the specified date and amount.
         *
         * @param date the date string in "yyyy-MM-dd" format
         * @param amount the total amount for the date
         */
        public DateSum(String date, double amount) {
            this.date = date;
            this.amount = amount;
        }

        public String getDate() { return date; }
        public double getAmount() { return amount; }
    }


}
