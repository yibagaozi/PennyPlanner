package org.softeng.group77.pennyplanner.service;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public interface TransactionAnalysisService {

    public CompletableFuture<String> classifyTransaction(String transactionDescription);

    public CompletableFuture<String> generateSpendingAnalysisReport(LocalDate startDate, LocalDate endDate);

    public CompletableFuture<Map<String, Object>> chatWithFinancialAssistant(String userMessage, List<Map<String, String>> conversationHistory, LocalDate startDate, LocalDate endDate);


}
