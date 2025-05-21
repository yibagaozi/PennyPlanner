package org.softeng.group77.pennyplanner.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.softeng.group77.pennyplanner.service.TransactionAnalysisService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransactionAnalysisServiceImpl implements TransactionAnalysisService {

    private final ChatClient chatClient;
    private final ExecutorService executorService;

    private static final List<String> predefinedCategories = List.of(
            "Food", "Transportation", "Education", "Entertainment", "Living Bill", "Clothes", "Others"
    );

    @Autowired
    public TransactionAnalysisServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                                .build();
        this.executorService = Executors.newCachedThreadPool();
        log.info("AzureOpenAIService initialized with Spring AI ChatClient.");
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

}
