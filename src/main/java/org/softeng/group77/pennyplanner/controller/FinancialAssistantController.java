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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private TransactionAnalysisService transactionAnalysisService;

    @Autowired
    private ApplicationContext applicationContext;

    private List<Map<String, String>> conversationHistory = new ArrayList<>();
    private static final String WELCOME_MESSAGE = "Hello! I'm a PennyPlanner Finance Assistant. Please select a date range and let me help you analyze your finances and answer your questions。";
    public static void setPreviousView(String viewName) {
        previousView = viewName;
    }

    @FXML
    private void initialize() {
        // 初始化日期选择为当前月份
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.withDayOfMonth(1);
        startDatePicker.setValue(firstDayOfMonth);
        endDatePicker.setValue(now);

        // 设置按钮事件
        sendButton.setOnAction(e -> sendMessage());

        // 设置回车键发送
        messageField.setOnAction(e -> sendMessage());

        // 添加常用问题按钮
        setupQuickQuestions();

        // 显示欢迎消息
        Platform.runLater(() -> {
            addAssistantMessage(WELCOME_MESSAGE);
        });
    }

    private void setupQuickQuestions() {
        String[] questions = {
                "我的月度支出分布如何？",
                "哪个类别花费最多？",
                "如何优化我的预算？",
                "我的收支趋势如何？",
                "如何提高我的储蓄率？"
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

    @FXML
    private void sendMessage() {
        String message = messageField.getText().trim();
        if (message.isEmpty()) {
            return;
        }

        // 显示用户消息
        addUserMessage(message);

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

                            // 更新对话历史
                            conversationHistory = (List<Map<String, String>>) response.get("history");
                        } else {
                            // 显示错误信息
                            String errorMsg = (String) response.get("error");
                            addSystemMessage("发生错误: " + errorMsg);
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        toggleLoading(false);
                        addSystemMessage("请求失败: " + ex.getMessage());
                    });
                    return null;
                });
    }

    private void toggleLoading(boolean isLoading) {
        sendButton.setDisable(isLoading);
        loadingIndicator.setVisible(isLoading);
    }

    private void addUserMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_RIGHT);

        VBox bubble = createBubble(message, "#dcf8c6", "#303030");

        messageBox.getChildren().add(bubble);
        HBox.setMargin(bubble, new Insets(5, 5, 5, 80));

        chatContainer.getChildren().add(messageBox);
        scrollToBottom();
    }

    private void addAssistantMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_LEFT);

        VBox bubble = createBubble(message, "#ffffff", "#303030");

        messageBox.getChildren().add(bubble);
        HBox.setMargin(bubble, new Insets(5, 80, 5, 5));

        chatContainer.getChildren().add(messageBox);
        scrollToBottom();
    }

    private void addSystemMessage(String message) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER);

        Label systemMessage = new Label(message);
        systemMessage.setStyle("-fx-background-color: #ffe6e6; -fx-padding: 5px; -fx-background-radius: 5px; -fx-text-fill: #d32f2f;");

        messageBox.getChildren().add(systemMessage);

        chatContainer.getChildren().add(messageBox);
        scrollToBottom();
    }

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

    private void scrollToBottom() {
        Platform.runLater(() -> {
            if (chatContainer.getParent() instanceof ScrollPane) {
                ScrollPane scrollPane = (ScrollPane) chatContainer.getParent();
                scrollPane.setVvalue(1.0);
            }
        });
    }

    // 返回前一个页面
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

    @FXML
    private void turntoHome() throws java.io.IOException {
        MainApp.showHome();
    }

    @FXML
    private void turntoReport() throws java.io.IOException {
        MainApp.showReport();
    }

    @FXML
    private void turntoHistory() throws java.io.IOException {
        MainApp.showhistory();
    }

    @FXML
    private void turntoManagement() throws java.io.IOException {
        MainApp.showmanagement();
    }

    @FXML
    private void turntoUser() throws java.io.IOException {
        MainApp.showuser();
    }
}