package org.softeng.group77.pennyplanner.util;

import org.softeng.group77.pennyplanner.controller.tableModel;
import org.softeng.group77.pennyplanner.adapter.TransactionAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class CsvImporter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Set<String> VALID_CATEGORIES = new HashSet<>(Arrays.asList(
            "Food", "Salary", "Living Bill", "Entertainment", "Transportation",
            "Education", "Clothes", "Others"));

    private static final Set<String> VALID_PAYMENT_METHODS = new HashSet<>(Arrays.asList(
            "Credit Card", "Bank Transfer", "Auto-Payment", "Cash", "E-Payment"));

    /**
     * 解析CSV文件并返回导入结果
     * @param csvFile CSV文件
     * @return 导入结果包括成功导入的记录和错误信息
     */
    public static ImportResult importTransactions(File csvFile, TransactionAdapter transactionAdapter) {
        List<tableModel> successfulImports = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;

            // 读取表头
            line = reader.readLine();
            lineNumber++;

            if (line == null) {
                errorMessages.add("CSV文件为空");
                return new ImportResult(successfulImports, errorMessages);
            }

            // 验证表头格式
            String[] headers = line.split(",");
            if (headers.length != 5 ||
                    !headers[0].trim().equalsIgnoreCase("date") ||
                    !headers[1].trim().equalsIgnoreCase("description") ||
                    !headers[2].trim().equalsIgnoreCase("amount") ||
                    !headers[3].trim().equalsIgnoreCase("category") ||
                    !headers[4].trim().equalsIgnoreCase("method")) {

                errorMessages.add("CSV表头格式不正确。正确格式应为: date,description,amount,category,method");
                return new ImportResult(successfulImports, errorMessages);
            }

            // 处理数据行
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;

                try {
                    String[] fields = line.split(",");
                    if (fields.length != 5) {
                        errorMessages.add("第" + lineNumber + "行: 格式不正确，字段数量应为5");
                        continue;
                    }

                    // 1. 验证和解析日期
                    String dateStr = fields[0].trim();
                    try {
                        LocalDate.parse(dateStr, DATE_FORMATTER);
                    } catch (DateTimeParseException e) {
                        errorMessages.add("第" + lineNumber + "行: 日期格式无效，应为YYYY-MM-DD");
                        continue;
                    }

                    // 2. 获取描述
                    String description = fields[1].trim();
                    if (description.isEmpty()) {
                        description = "Imported transaction"; // 提供默认描述
                    }

                    // 3. 解析金额
                    double amount;
                    try {
                        amount = Double.parseDouble(fields[2].trim());
                    } catch (NumberFormatException e) {
                        errorMessages.add("第" + lineNumber + "行: 金额格式无效");
                        continue;
                    }

                    // 4. 验证类别
                    String category = fields[3].trim();
                    if (!VALID_CATEGORIES.contains(category)) {
                        errorMessages.add("第" + lineNumber + "行: 无效的类别 '" + category + "'");
                        continue;
                    }

                    // 5. 验证支付方式
                    String method = fields[4].trim();
                    if (!VALID_PAYMENT_METHODS.contains(method)) {
                        errorMessages.add("第" + lineNumber + "行: 无效的支付方式 '" + method + "'");
                        continue;
                    }

                    // 创建交易记录
                    String id = UUID.randomUUID().toString();
                    tableModel transaction = new tableModel(
                            id,              // 生成新ID
                            dateStr,         // 日期
                            description,     // 描述
                            amount,          // 金额
                            category,        // 类别
                            method           // 支付方式
                    );

                    // 保存记录到数据库
                    boolean saved = transactionAdapter.saveTransaction(transaction);
                    if (saved) {
                        successfulImports.add(transaction);
                    } else {
                        errorMessages.add("第" + lineNumber + "行: 保存记录失败");
                    }

                } catch (Exception e) {
                    errorMessages.add("第" + lineNumber + "行: 处理失败 - " + e.getMessage());
                }
            }

        } catch (IOException e) {
            errorMessages.add("读取CSV文件失败: " + e.getMessage());
        }

        return new ImportResult(successfulImports, errorMessages);
    }

    /**
     * 导入结果类
     */
    public static class ImportResult {
        private final List<tableModel> successfulImports;
        private final List<String> errorMessages;

        public ImportResult(List<tableModel> successfulImports, List<String> errorMessages) {
            this.successfulImports = successfulImports;
            this.errorMessages = errorMessages;
        }

        public List<tableModel> getSuccessfulImports() {
            return successfulImports;
        }

        public List<String> getErrorMessages() {
            return errorMessages;
        }

        public int getTotalSuccessful() {
            return successfulImports.size();
        }

        public int getTotalErrors() {
            return errorMessages.size();
        }

        public boolean hasErrors() {
            return !errorMessages.isEmpty();
        }
    }
}