package org.softeng.group77.pennyplanner.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.softeng.group77.pennyplanner.service.TransactionAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.softeng.group77.pennyplanner.controller.SharedDataModel;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;


import java.io.IOException;
import java.time.LocalDate;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for the financial report generation view in the PennyPlanner application.
 * This controller handles the generation of AI-powered financial reports based on
 * user-selected date ranges.
 * The controller uses the TransactionAnalysisService to process financial data
 * and generate analytical reports.
 *
 * @author WANG Bingsong
 * @version 2.0.0
 * @since 1.2.0
 */
@Controller
public class ReportController {
    @FXML
    private TextArea myTextArea; // 必须与FXML中的fx:id一致

    @FXML
    private Button generateButton; // 生成按钮

    @FXML
    private DatePicker startDatePicker; // 开始日期选择器

    @FXML
    private DatePicker endDatePicker; // 结束日期选择器

    @FXML
    private ProgressIndicator progressIndicator; // 进度指示器

    private TransactionAnalysisService transactionAnalysisService;

    /**
     * Sets the transaction analysis service
     *
     * @param transactionAnalysisService the service for generating AI-powered reports
     */
    @Autowired
    public void setTransactionAnalysisService(TransactionAnalysisService transactionAnalysisService) {
        this.transactionAnalysisService = transactionAnalysisService;
    }

    @FXML
    private SplitPane splitPane;

    /**
     * Initializes the controller after FXML elements are loaded.
     * Sets up the UI with default values, date ranges, and layout constraints.
     */
    @FXML
    private void initialize() {
        Locale.setDefault(Locale.ENGLISH);
        // 设置初始文本
        String text="Choose date range and click the button to generate a report";
        myTextArea.setText(text);
        myTextArea.setWrapText(true);

        // 禁用分割线的拖动
        splitPane.getDividers().forEach(divider -> divider.positionProperty().addListener((observable, oldValue, newValue) -> {
            divider.setPosition(0.12); // 固定分割线位置为 10%
        }));

        // 设置日期选择器的默认值
        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());

        // 初始隐藏进度指示器
        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
        }

    }

    /**
     * Navigates to the home view
     *
     * @throws IOException if navigation fails
     */
    @FXML
    private void turntoHome() throws IOException {
        MainApp.showHome();
    }

    /**
     * Navigates to the report view
     *
     * @throws IOException if navigation fails
     */
    @FXML
    private void turntoReport() throws IOException {
        MainApp.showReport();
    }

    /**
     * Navigates to the history view
     *
     * @throws IOException if navigation fails
     */
    @FXML
    private void turntoHistory() throws IOException {
        MainApp.showhistory();
    }

    /**
     * Navigates to the management view
     *
     * @throws IOException if navigation fails
     */
    @FXML
    private void turntoManagement() throws IOException {
        MainApp.showmanagement();
    }

    /**
     * Navigates to the user profile view
     *
     * @throws IOException if navigation fails
     */
    @FXML
    private void turntoUser() throws IOException {
        MainApp.showuser();
    }

    /**
     * Navigates to the login view
     *
     * @throws IOException if navigation fails
     */
    @FXML
    private void turntoLogin() throws IOException {
        System.out.println("Login");
        MainApp.showLogin();
    }

    /**
     * Navigates to the financial assistant view
     *
     * @throws IOException if navigation fails
     */
    @FXML
    private void turntoFinancialAssistant() throws IOException {
        MainApp.showFinancialAssistant();
    }

    /**
     * Generates an AI-powered financial report based on the selected date range.
     * This method validates the date range, then asynchronously calls the
     * TransactionAnalysisService to generate a detailed spending analysis report.
     * During report generation, a progress indicator is displayed and the UI is
     * updated when the report is complete.
     *
     * @throws IOException if there is an error during report generation
     */
    @FXML
    private void useAI() throws IOException {
        System.out.println("Call API for AI-generated content");
        // 检查日期是否有效
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null) {
            showAlert("Invalid Date", "Please Choose Valid Start and End Date", Alert.AlertType.WARNING);
            return;
        }

        if (startDate.isAfter(endDate)) {
            showAlert("Invalid Date", "Start date must be earlier than end date.", Alert.AlertType.WARNING);
            return;
        }

        // 显示进度指示器并禁用生成按钮
        progressIndicator.setVisible(true);
        generateButton.setDisable(true);
        myTextArea.setText("AI's generating report...");

        // 异步调用AI分析服务
        CompletableFuture<String> reportFuture = transactionAnalysisService.generateSpendingAnalysisReport(startDate, endDate);

        reportFuture.thenAccept(report -> {
            // 在JavaFX UI线程中更新UI
            Platform.runLater(() -> {
                myTextArea.setText(report);
                progressIndicator.setVisible(false);
                generateButton.setDisable(false);
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                myTextArea.setText("Error when generating " + ex.getMessage());
                showAlert("Error", "Failed to generate " + ex.getMessage(), Alert.AlertType.ERROR);
                endDatePicker.setVisible(false);
                generateButton.setDisable(false);
            });
            return null;
        });
    }

    /**
     * Displays an alert dialog with the specified title, message, and type
     *
     * @param title the title of the alert
     * @param message the message content
     * @param alertType the type of alert to display
     */
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
