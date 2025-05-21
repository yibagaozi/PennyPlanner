package org.softeng.group77.pennyplanner.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.softeng.group77.pennyplanner.dto.TransactionDetail;
import org.softeng.group77.pennyplanner.model.Transaction;
import org.softeng.group77.pennyplanner.service.TransactionAnalysisService;
import org.softeng.group77.pennyplanner.service.TransactionService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransactionAnalysisServiceImpl implements TransactionAnalysisService {

    private final ChatClient chatClient;
    private final ExecutorService executorService;
    private final TransactionService transactionService;

    private static final List<String> predefinedCategories = List.of(
            "Food", "Transportation", "Education", "Entertainment", "Living Bill", "Clothes", "Others"
    );

    @Autowired
    public TransactionAnalysisServiceImpl(ChatClient.Builder chatClientBuilder, TransactionService transactionService) {
        this.chatClient = chatClientBuilder
                                .build();
        this.executorService = Executors.newCachedThreadPool();
        log.info("AzureOpenAIService initialized with Spring AI ChatClient.");
        this.transactionService = transactionService;
    }

    @Override
    public CompletableFuture<String> classifyTransaction(String transactionDescription) {
        if (transactionDescription == null || transactionDescription.trim().isEmpty()) {
            return CompletableFuture.completedFuture("Description is empty");
        }
        if (predefinedCategories == null || predefinedCategories.isEmpty()) {
            return CompletableFuture.completedFuture("No categories available");
        }

        String categoriesString = predefinedCategories.stream()
                .map(c -> "\"" + c + "\"")
                .collect(Collectors.joining(", "));

        String systemPromptText = String.format(
            "You are an intelligent accounting assistant responsible for accurately categorizing user's expense records. " +
            "Based on the user's expense description, please select the most appropriate category from the following predefined list: %s.\n" +
            "Please return only one category name from the list without adding any explanations, punctuation marks, or quotation marks.\n" +
            "Example 1 (assuming 'Utilities' is in the category list):\nUser description: Paid electricity bill\nYour answer: Utilities\n" +
            "Example 2 (assuming 'Food & Dining' is in the category list):\nUser description: Purchased vegetables and fruits\nYour answer: Food & Dining",
            categoriesString);

        String userPromptText = "Transaction Description：" + transactionDescription;

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Per-request options if needed, otherwise global config is used
                OpenAiChatOptions options = OpenAiChatOptions.builder()
                        .temperature(0.2)
                        .maxTokens(500)
                        .build();

                String rawCategory = chatClient.prompt()
                        .options(options)
                        .system(systemPromptText)
                        .user(userPromptText)
                        .call()
                        .content(); // Directly get the content string

                if (rawCategory != null) {
                    String cleanedCategory = rawCategory.trim().replaceAll("\"", "");
                    return predefinedCategories.stream()
                            .filter(c -> c.equalsIgnoreCase(cleanedCategory))
                            .findFirst()
                            .orElse("Waiting for classification (Advice: " + cleanedCategory + ")");
                }
                log.warn("Classification returned null content for description: {}", transactionDescription);
                return "Classification failed";
            } catch (Exception e) {
                log.error("Error during OpenAI call for classification [{}]: {}", transactionDescription, e.getMessage(), e);
                return "Failed to call classification service";
            }
        }, executorService);
    }

    /**
 * 生成指定日期范围内的消费分析报告和预测
 *
 * @param startDate 开始日期
 * @param endDate 结束日期
 * @return 包含分析报告的CompletableFuture
 */
public CompletableFuture<String> generateSpendingAnalysisReport(LocalDate startDate, LocalDate endDate) {
    if (startDate == null || endDate == null) {
        return CompletableFuture.completedFuture("Date range cannot be null");
    }

    if (startDate.isAfter(endDate)) {
        return CompletableFuture.completedFuture("Start date cannot be after end date");
    }

    return CompletableFuture.supplyAsync(() -> {
        try {
            // 1. 从交易服务获取数据
            List<Transaction> transactions = transactionService.filterTransactionByDateForAnalysis(startDate, endDate);
            Map<String, Double> transactionData = transactionService.getSummaryByDateRange(startDate, endDate);

            if (transactions.isEmpty()) {
                return "No transactions found in the specified date range.";
            }

            Map<String, BigDecimal> categoryExpenseSummary = new HashMap<>();

            for (Transaction transaction : transactions) {
                if (transaction.getAmount().compareTo(BigDecimal.ZERO) < 0) { // 支出是负值
                    String category = transaction.getCategory();
                    // 使用 abs() 方法获取绝对值
                    BigDecimal amount = transaction.getAmount().abs();

                    categoryExpenseSummary.put(
                        category,
                        categoryExpenseSummary.getOrDefault(category, BigDecimal.ZERO).add(amount)
                    );
                }
            }

            Double totalIncome = transactionData.getOrDefault("income", 0.0);
            Double totalExpense = transactionData.getOrDefault("expense", 0.0);
            Double totalBalance = transactionData.getOrDefault("totalBalance", 0.0);

            // 转换为格式化字符串
            StringBuilder transactionSummary = new StringBuilder();
            transactionSummary.append("Date Range: ").append(startDate).append(" to ").append(endDate).append("\n");
            transactionSummary.append("Total Income: $").append(String.format("%.2f", totalIncome)).append("\n");
            transactionSummary.append("Total Expense: $").append(String.format("%.2f", totalExpense)).append("\n");
            transactionSummary.append("Total Balance: $").append(String.format("%.2f", totalBalance)).append("\n\n");

            transactionSummary.append("Expense Breakdown by Category:\n");
            categoryExpenseSummary.forEach((category, amount) -> {

                BigDecimal percentage = amount.multiply(new BigDecimal("100"))
                    .divide(new BigDecimal(totalExpense.toString()), 2, RoundingMode.HALF_UP);

                transactionSummary.append("- ").append(category).append(": $")
                    .append(amount.setScale(2, RoundingMode.HALF_UP).toString())
                    .append(" (").append(percentage.toString()).append("%)\n");
                });

            transactionSummary.append("\nTransaction Timeline:\n");
            transactions.stream()
                .sorted(Comparator.comparing(Transaction::getTransactionDateTime).reversed())
                .limit(20)
                .forEach(t -> {
                    // 格式化 BigDecimal 金额，保留两位小数
                    String formattedAmount = t.getAmount().abs().setScale(2, RoundingMode.HALF_UP).toString();

                    // 使用 compareTo 判断金额是收入还是支出
                    String transactionType = t.getAmount().compareTo(BigDecimal.ZERO) < 0 ? "Expense" : "Income";

                    transactionSummary.append("- ").append(t.getTransactionDateTime())
                        .append(": ").append(t.getDescription())
                        .append(" - $").append(formattedAmount)
                        .append(" (").append(transactionType).append(")\n");
                });

            // 3. 构建系统提示
            String systemPromptText = String.format(
                    "You are a personal finance analyst and advisor with expertise in budgeting and financial planning. " +
                            "Based on the transaction data provided, analyze the spending patterns, identify trends, and provide " +
                            "actionable insights. Your analysis should include:\n" +
                            "1. A summary of spending habits across different categories\n" +
                            "2. Identification of any unusual spending or potential areas to save money\n" +
                            "3. Comparison to typical spending patterns for similar time periods\n" +
                            "4. Specific, personalized recommendations for budgeting and saving\n" +
                            "5. Short-term financial predictions for the next month based on current trends\n\n" +
                            "Format your response as a professional financial report with clear sections and bullet points where appropriate. " +
                            "Use the actual numbers provided in your analysis."
            );

            // 4. 构建用户提示(包含交易数据)
            String userPromptText = "Please analyze the following transaction data and generate a spending report:\n\n" +
                    transactionSummary.toString();

            // 5. 调用OpenAI
            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .temperature(0.7) // 稍微提高创造性
                    .maxTokens(3000) // 允许更长的回复
                    .build();

            String report = chatClient.prompt()
                    .options(options)
                    .system(systemPromptText)
                    .user(userPromptText)
                    .call()
                    .content();

            if (report != null && !report.trim().isEmpty()) {
                return report;
            }

            log.warn("Analysis report generation returned empty content for date range: {} to {}");
            return "Failed to generate spending analysis report";

            } catch (Exception e) {
                log.error("Error during spending analysis report generation [{} to {}]: {}");
                return "Failed to generate spending analysis report";
            }
        }, executorService);
    }

}
