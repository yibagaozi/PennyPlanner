package org.softeng.group77.pennyplanner.service;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
/**
 * Provides AI-powered transaction analysis and financial assistant services.
 * Handles transaction classification, spending analysis, and conversational assistance.
 *
 * @author MA Ruize
 * @version 2.0.0
 * @since 2.0.0
 */

@Component
public interface TransactionAnalysisService {

    /**
     * Classifies a transaction description into a recommended category
     *
     * @param transactionDescription the description text to analyze
     * @return a future containing the recommended category name
     */
    public CompletableFuture<String> classifyTransaction(String transactionDescription);

    /**
     * Generates a comprehensive spending analysis report for a date range
     *
     * @param startDate the beginning date of analysis period
     * @param endDate the ending date of analysis period
     * @return a future containing the formatted analysis report
     */
    public CompletableFuture<String> generateSpendingAnalysisReport(LocalDate startDate, LocalDate endDate);

    /**
     * Provides an AI financial assistant chat interface
     *
     * @param userMessage the user's current query
     * @param conversationHistory previous messages for context
     * @param startDate optional start date for data analysis
     * @param endDate optional end date for data analysis
     * @return a future containing the assistant response and any data
     */
    public CompletableFuture<Map<String, Object>> chatWithFinancialAssistant(String userMessage, List<Map<String, String>> conversationHistory, LocalDate startDate, LocalDate endDate);


}
