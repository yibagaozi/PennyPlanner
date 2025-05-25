package org.softeng.group77.pennyplanner.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import lombok.extern.slf4j.Slf4j;
import org.softeng.group77.pennyplanner.service.TransactionAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

/**
 * Controller for the Financial Assistant chat interface in PennyPlanner.
 * This controller manages an AI-powered chat interface that allows users to:
 * The controller maintains conversation history between views and uses the
 * TransactionAnalysisService to process user queries and generate responses.
 *
 * @author WANG Bingsong
 * @version 2.0.0
 * @since 2.0.0
 */
@Controller
@Slf4j
public class FinancialAssistantController {

    @FXML private TextField messageField;
    @FXML private Button sendButton;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private VBox chatContainer;
    @FXML private FlowPane quickQuestionsPane;
    @FXML private ProgressIndicator loadingIndicator;

    private static String previousView = "";

    // 静态变量保存聊天记录和对话历史
    private static List<Map<String, String>> savedConversationHistory = new ArrayList<>();
    private static List<ChatMessage> savedChatMessages = new ArrayList<>();
    private static LocalDate savedStartDate;
    private static LocalDate savedEndDate;
    private static boolean hasInitialized = false;

    @Autowired
    private TransactionAnalysisService transactionAnalysisService;

    @Autowired
    private ApplicationContext applicationContext;

    private List<Map<String, String>> conversationHistory = new ArrayList<>();
    private static final String WELCOME_MESSAGE = "Hello! I'm PennyPlanner, your finance assistant. Please select a date range and let me help you analyze your finances and answer your questions.";

    /**
     * Internal class to store chat messages with their type
     */
    private static class ChatMessage {
        String content;
        String type; // "user", "assistant", "system"

        /**
         * Creates a new chat message
         *
         * @param content the message content
         * @param type the message type (user, assistant, or system)
         */
        ChatMessage(String content, String type) {
            this.content = content;
            this.type = type;
        }
    }

    /**
     * Sets the previous view name for navigation purposes
     *
     * @param viewName the name of the previous view
     */
    public static void setPreviousView(String viewName) {
        previousView = viewName;
    }

    /**
     * Initializes the controller after FXML elements are loaded.
     * Sets up date pickers, event handlers, and restores previous chat history if available.
     */
    @FXML
    private void initialize() {
        Locale.setDefault(Locale.ENGLISH);
        // 初始化日期选择为之前保存的日期或当前月份
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.withDayOfMonth(1);

        if (savedStartDate != null && savedEndDate != null) {
            startDatePicker.setValue(savedStartDate);
            endDatePicker.setValue(savedEndDate);
        } else {
            startDatePicker.setValue(firstDayOfMonth);
            endDatePicker.setValue(now);
        }

        // 设置按钮事件
        sendButton.setOnAction(e -> sendMessage());

        // 设置回车键发送
        messageField.setOnAction(e -> sendMessage());

        // 添加常用问题按钮
        setupQuickQuestions();

        // 恢复或显示聊天记录
        Platform.runLater(() -> {
            if (!savedChatMessages.isEmpty()) {
                // 恢复之前的聊天记录
                restoreChatMessages();
                conversationHistory = new ArrayList<>(savedConversationHistory);
            } else if (!hasInitialized) {
                // 首次加载显示欢迎消息
                addAssistantMessage(WELCOME_MESSAGE);
                hasInitialized = true;
            }
        });

        // 监听日期选择器变化
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            savedStartDate = newVal;
        });

        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            savedEndDate = newVal;
        });
    }

    /**
     * Restores previously saved chat messages to the chat container
     */
    private void restoreChatMessages() {
        chatContainer.getChildren().clear();
        for (ChatMessage message : savedChatMessages) {
            switch (message.type) {
                case "user":
                    addUserMessage(message.content);
                    break;
                case "assistant":
                    addAssistantMessage(message.content);
                    break;
                case "system":
                    addSystemMessage(message.content);
                    break;
            }
        }
    }

    /**
     * Sets up quick question buttons for common financial inquiries
     */
    private void setupQuickQuestions() {
        String[] questions = {
                "Give advice on possible upcoming spending spikes",
                "Analyse the trend of my income and expenditure",
                "Analyze my spending behavior and give suggestions",
                "How can I increase my savings rate?"
        };

        for (String question : questions) {
            Button questionButton = new Button(question);
            questionButton.setStyle("-fx-background-color: #e8f4f8; -fx-text-fill: #2c7bb6;");
            questionButton.setOnAction(e -> {
                messageField.setText(question);
                sendMessage();
            });
            quickQuestionsPane.getChildren().add(questionButton);
        }
    }

    /**
     * Sends the current message from the input field to the AI service
     * and displays the response in the chat container
     */
    @FXML
    private void sendMessage() {
        String message = messageField.getText().trim();
        if (message.isEmpty()) {
            return;
        }

        // 显示用户消息
        addUserMessage(message);

        // 保存用户消息到静态记录
        savedChatMessages.add(new ChatMessage(message, "user"));

        // 清空输入框
        messageField.clear();

        // 禁用发送按钮并显示加载指示器
        toggleLoading(true);

        // 获取日期范围
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        // 调用AI服务
        transactionAnalysisService.chatWithFinancialAssistant(message, conversationHistory, startDate, endDate)
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        toggleLoading(false);

                        if ((Boolean) response.get("success")) {
                            // 显示助手回复
                            String aiResponse = (String) response.get("response");
                            addAssistantMessage(aiResponse);

                            // 保存助手回复到静态记录
                            savedChatMessages.add(new ChatMessage(aiResponse, "assistant"));

                            // 更新对话历史
                            conversationHistory = (List<Map<String, String>>) response.get("history");
                            savedConversationHistory = new ArrayList<>(conversationHistory);
                        } else {
                            // 显示错误信息
                            String errorMsg = (String) response.get("error");
                            addSystemMessage("发生错误: " + errorMsg);

                            // 保存错误信息到静态记录
                            savedChatMessages.add(new ChatMessage("发生错误: " + errorMsg, "system"));
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        toggleLoading(false);
                        addSystemMessage("请求失败: " + ex.getMessage());

                        // 保存错误信息到静态记录
                        savedChatMessages.add(new ChatMessage("请求失败: " + ex.getMessage(), "system"));
                    });
                    return null;
                });
    }

    /**
     * Toggles the loading state - disables the send button and shows/hides the loading indicator
     *
     * @param isLoading true to show loading state, false to hide it
     */
    private void toggleLoading(boolean isLoading) {
        sendButton.setDisable(isLoading);
        loadingIndicator.setVisible(isLoading);
    }

    /**
     * Adds a user message to the chat container
     *
     * @param message the message content to display
     */
    private void addUserMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_RIGHT);

        VBox bubble = createBubble(message, "#dcf8c6", "#303030");

        messageBox.getChildren().add(bubble);
        HBox.setMargin(bubble, new Insets(5, 5, 5, 80));

        chatContainer.getChildren().add(messageBox);
        scrollToBottom();
    }

    /**
     * Adds an assistant message to the chat container
     *
     * @param message the message content to display
     */
    private void addAssistantMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_LEFT);

        VBox bubble = createBubble(message, "#ffffff", "#303030");

        messageBox.getChildren().add(bubble);
        HBox.setMargin(bubble, new Insets(5, 80, 5, 5));

        chatContainer.getChildren().add(messageBox);
        scrollToBottom();
    }

    /**
     * Adds a system message to the chat container
     *
     * @param message the system message content to display
     */
    private void addSystemMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER);

        Label systemMessage = new Label(message);
        systemMessage.setStyle("-fx-background-color: #ffe6e6; -fx-padding: 5px; -fx-background-radius: 5px; -fx-text-fill: #d32f2f;");

        messageBox.getChildren().add(systemMessage);

        chatContainer.getChildren().add(messageBox);
        scrollToBottom();
    }

    /**
     * Creates a message bubble with the specified styling
     *
     * @param message the message content
     * @param bgColor the background color for the bubble
     * @param textColor the text color for the message
     * @return a styled VBox containing the message
     */
    private VBox createBubble(String message, String bgColor, String textColor) {
        VBox bubble = new VBox();
        bubble.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 10;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 1, 1, 0, 1);"
        );
        bubble.setMaxWidth(500);

        TextFlow textFlow = new TextFlow();
        Text text = new Text(message);
        text.setFill(Color.web(textColor));
        text.setStyle("-fx-font-size: 14px;");

        textFlow.getChildren().add(text);
        bubble.getChildren().add(textFlow);

        return bubble;
    }

    /**
     * Scrolls the chat container to the bottom to show the most recent message
     */
    private void scrollToBottom() {
        Platform.runLater(() -> {
            if (chatContainer.getParent() instanceof ScrollPane) {
                ScrollPane scrollPane = (ScrollPane) chatContainer.getParent();
                scrollPane.setVvalue(1.0);
            }
        });
    }

    /**
     * Clears all chat history and conversation state
     */
    public static void clearChat() {
        savedChatMessages.clear();
        savedConversationHistory.clear();
        hasInitialized = false;
    }

    /**
     * Navigates back to the previous view
     *
     * @throws IOException if navigation fails
     */
    @FXML
    private void goBack() throws IOException {
        switch (previousView) {
            case "home":
                MainApp.showHome();
                break;
            case "history":
                MainApp.showhistory();
                break;
            case "report":
                MainApp.showReport();
                break;
            case "management":
                MainApp.showmanagement();
                break;
            case "user":
                MainApp.showuser();
                break;
            default:
                MainApp.showHome();
                break;
        }
    }

    /**
     * Navigates to the home view
     *
     * @throws IOException if navigation fails
     */
    @FXML
    private void turntoHome() throws java.io.IOException {
        MainApp.showHome();
    }

    /**
     * Navigates to the report view
     *
     * @throws IOException if navigation fails
     */
    @FXML
    private void turntoReport() throws java.io.IOException {
        MainApp.showReport();
    }

    /**
     * Navigates to the history view
     *
     * @throws IOException if navigation fails
     */
    @FXML
    private void turntoHistory() throws java.io.IOException {
        MainApp.showhistory();
    }

    /**
     * Navigates to the management view
     *
     * @throws IOException if navigation fails
     */
    @FXML
    private void turntoManagement() throws java.io.IOException {
        MainApp.showmanagement();
    }

    /**
     * Navigates to the user profile view
     *
     * @throws IOException if navigation fails
     */
    @FXML
    private void turntoUser() throws java.io.IOException {
        MainApp.showuser();
    }
}