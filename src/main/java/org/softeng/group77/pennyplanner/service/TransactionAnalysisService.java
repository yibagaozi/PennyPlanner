package org.softeng.group77.pennyplanner.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public interface TransactionAnalysisService {

    public CompletableFuture<String> classifyTransaction(String transactionDescription);

}
