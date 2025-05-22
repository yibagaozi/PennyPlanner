package org.softeng.group77.pennyplanner.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.softeng.group77.pennyplanner.dto.TransactionDetail;
import org.softeng.group77.pennyplanner.model.Transaction;
import org.softeng.group77.pennyplanner.service.TransactionAnalysisService;
import org.softeng.group77.pennyplanner.service.TransactionService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
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
     * Generate the spending analysis report for a given date range.
     *
     * @param startDate Start date for transactions
     * @param endDate End date for transactions
     * @return CompletableFuture contains analysis report
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
                List<Transaction> transactions = transactionService.filterTransactionByDateForAnalysis(startDate, endDate);
                Map<String, Double> transactionData = transactionService.getSummaryByDateRange(startDate, endDate);

                if (transactions.isEmpty()) {
                    return "No transactions found in the specified date range.";
                }

                Map<String, BigDecimal> categoryExpenseSummary = new HashMap<>();

                for (Transaction transaction : transactions) {
                    if (transaction.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                        String category = transaction.getCategory();
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
                        String formattedAmount = t.getAmount().abs().setScale(2, RoundingMode.HALF_UP).toString();

                        String transactionType = t.getAmount().compareTo(BigDecimal.ZERO) < 0 ? "Expense" : "Income";

                        transactionSummary.append("- ").append(t.getTransactionDateTime())
                            .append(": ").append(t.getDescription())
                            .append(" - $").append(formattedAmount)
                            .append(" (").append(transactionType).append(")\n");
                    });

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

                String userPromptText = "Please analyze the following transaction data and generate a spending report:\n\n" +
                        transactionSummary.toString();

                OpenAiChatOptions options = OpenAiChatOptions.builder()
                        .temperature(0.7)
                        .maxTokens(3000)
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

    /**
     * Chat with the financial assistant method supporting context.
     *
     * @param userMessage User message
     * @param conversationHistory Chat history
     * @param startDate Transaction analysis start date
     * @param endDate Transaction analysis end date
     * @return Response
     */
    @Override
    public CompletableFuture<Map<String, Object>> chatWithFinancialAssistant(
            String userMessage,
            List<Map<String, String>> conversationHistory,
            LocalDate startDate,
            LocalDate endDate) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                if (userMessage == null || userMessage.trim().isEmpty()) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("error", "User message cannot be empty");
                    errorResponse.put("history", conversationHistory);
                    return errorResponse;
                }

                List<Map<String, String>> history = conversationHistory != null
                    ? new ArrayList<>(conversationHistory)
                    : new ArrayList<>();

                Map<String, String> userEntry = new HashMap<>();
                userEntry.put("role", "user");
                userEntry.put("content", userMessage);
                history.add(userEntry);

                String systemPrompt = buildSystemPrompt(startDate, endDate);

                String transactionContext = "";
                if (startDate != null && endDate != null) {
                    transactionContext = prepareTransactionContext(startDate, endDate);
                }

                if (history.isEmpty() || !"system".equals(history.get(0).get("role"))) {
                    Map<String, String> systemEntry = new HashMap<>();
                    systemEntry.put("role", "system");
                    systemEntry.put("content", systemPrompt);
                    history.add(0, systemEntry);
                }

                if (!transactionContext.isEmpty()) {
                    Map<String, String> contextEntry = new HashMap<>();
                    contextEntry.put("role", "system");
                    contextEntry.put("content", "Here is the user's transaction data for analysis:\n" + transactionContext);
                    history.add(1, contextEntry);
                }

                List<Message> messages = history.stream()
                    .map(entry -> {
                        String role = entry.get("role");
                        String content = entry.get("content");

                        if (content == null) {
                            content = "";
                        }

                        return switch (role) {
                            case "system" -> new SystemMessage(content);
                            case "assistant" -> new AssistantMessage(content);
                            default -> new UserMessage(content);
                        };
                    })
                    .collect(Collectors.toList());

                Prompt prompt = new Prompt(messages);
                String response = chatClient.prompt(prompt).call().content();

                Map<String, String> assistantEntry = new HashMap<>();
                assistantEntry.put("role", "assistant");
                assistantEntry.put("content", response);
                history.add(assistantEntry);

                Map<String, Object> successResponse = new HashMap<>();
                successResponse.put("success", true);
                successResponse.put("response", response);
                successResponse.put("history", history);

                return successResponse;

            } catch (Exception e) {
                log.error("Error in chatWithFinancialAssistant: {}", e.getMessage(), e);

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", e.getMessage());
                errorResponse.put("history", conversationHistory);

                return errorResponse;
            }
        }, executorService);
    }

    private String buildSystemPrompt(LocalDate startDate, LocalDate endDate) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an intelligent financial assistant for PennyPlanner. ");
        prompt.append("You help users understand their spending patterns, provide financial advice, ");
        prompt.append("and answer questions about their transaction history. ");

        if (startDate != null && endDate != null) {
            prompt.append("You have access to the user's transaction data from ")
                  .append(startDate).append(" to ").append(endDate).append(". ");
            prompt.append("Analyze this data to provide personalized insights. ");
        }

        prompt.append("Be helpful, clear, and provide actionable financial advice when appropriate. ");
        prompt.append("Format your responses with markdown for readability. ");
        prompt.append("Current date: ").append(LocalDate.now()).append(".");

        return prompt.toString();
    }

    private String prepareTransactionContext(LocalDate startDate, LocalDate endDate) {
        try {
            List<Transaction> transactions = transactionService.filterTransactionByDateForAnalysis(startDate, endDate);
            if (transactions == null || transactions.isEmpty()) {
                return "No transaction data available for the specified period.";
            }

            Map<String, Double> transactionSummary= transactionService.getSummaryByDateRange(startDate, endDate);

            BigDecimal totalIncome = BigDecimal.valueOf(transactionSummary.getOrDefault("income", 0.0));
            BigDecimal totalExpense = BigDecimal.valueOf(transactionSummary.getOrDefault("expense", 0.0));

            Map<String, BigDecimal> categoryExpenseSummary = new HashMap<>();
            for (Transaction transaction : transactions) {
                if (transaction.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                    String category = transaction.getCategory();
                    BigDecimal amount = transaction.getAmount().abs();
                    categoryExpenseSummary.put(
                        category,
                        categoryExpenseSummary.getOrDefault(category, BigDecimal.ZERO).add(amount)
                    );
                }
            }

            StringBuilder summary = new StringBuilder();
            summary.append("## Transaction Data Summary\n\n");
            summary.append("Date Range: ").append(startDate).append(" to ").append(endDate).append("\n");
            summary.append("Total Income: $").append(totalIncome.setScale(2, RoundingMode.HALF_UP)).append("\n");
            summary.append("Total Expense: $").append(totalExpense.setScale(2, RoundingMode.HALF_UP)).append("\n");
            summary.append("Net Cash Flow: $").append(totalIncome.subtract(totalExpense).setScale(2, RoundingMode.HALF_UP)).append("\n\n");

            summary.append("### Expense Breakdown by Category:\n");
            categoryExpenseSummary.forEach((category, amount) -> {
                BigDecimal percentage = BigDecimal.ZERO;
                if (totalExpense.compareTo(BigDecimal.ZERO) > 0) {
                    percentage = amount.multiply(new BigDecimal("100"))
                        .divide(totalExpense, 2, RoundingMode.HALF_UP);
                }
                summary.append("- ").append(category).append(": $")
                    .append(amount.setScale(2, RoundingMode.HALF_UP))
                    .append(" (").append(percentage).append("%)\n");
            });

            summary.append("\n### Recent Transactions (up to 20):\n");
            transactions.stream()
                .sorted(Comparator.comparing(Transaction::getTransactionDateTime).reversed())
                .limit(20)
                .forEach(t -> {
                    String formattedAmount = t.getAmount().abs().setScale(2, RoundingMode.HALF_UP).toString();
                    String transactionType = t.getAmount().compareTo(BigDecimal.ZERO) < 0 ? "Expense" : "Income";

                    summary.append("- ").append(t.getTransactionDateTime())
                        .append(": ").append(t.getDescription())
                        .append(" - $").append(formattedAmount)
                        .append(" (").append(transactionType).append(")\n");
                });

            return summary.toString();

        } catch (Exception e) {
            log.error("Error preparing transaction context: {}", e.getMessage(), e);
            return "Error retrieving transaction data: " + e.getMessage();
        }
    }

}


