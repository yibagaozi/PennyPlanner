package org.softeng.group77.pennyplanner.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.softeng.group77.pennyplanner.service.TransactionAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Controller for the transaction classification window.
 * Handles AI-powered categorization of transaction descriptions.
 *
 * @author WANG Bingsong
 * @version 2.0.0
 * @since 2.0.0
 */
@Component
public class ClassificationWindowController {
    /**
     * Text field for entering transaction description
     */
    @FXML
    private TextField descriptionField;

    /**
     * Label displaying classification results or status messages
     */
    @FXML
    private Label resultLabel;

    /**
     * Button to trigger classification or confirm category selection
     */
    @FXML
    private Button classifyButton;

    /**
     * Button to cancel classification and close window
     */
    @FXML
    private Button cancelButton;

    /**
     * Service for transaction analysis and classification
     */
    private TransactionAnalysisService transactionAnalysisService;

    /**
     * Stores the AI-recommended category
     */
    private String classificationResult;

    /**
     * Tracks whether user confirmed the classification
     */
    private boolean confirmClicked = false;

    /**
     * Injects the transaction analysis service
     */
    @Autowired
    public void setTransactionAnalysisService(TransactionAnalysisService transactionAnalysisService) {
        this.transactionAnalysisService = transactionAnalysisService;
    }

    /**
     * Initializes the controller with default UI state
     */
    @FXML
    private void initialize() {
        resultLabel.setText("Type transaction description and click to classify");
    }

    /**
     * Handles classification button click.
     * Sends description to AI service and displays result.
     */
    @FXML
    private void handleClassify() {
        String description = descriptionField.getText().trim();
        if (description.isEmpty()) {
            resultLabel.setText("Type a description");
            return;
        }

        // 显示处理中状态
        classifyButton.setDisable(true);
        resultLabel.setText("Analyzing...");

        // 调用AI分类服务
        transactionAnalysisService.classifyTransaction(description)
                .thenAccept(category -> {
                    Platform.runLater(() -> {
                        classificationResult = category;
                        resultLabel.setText("Recommended category: " + category);
                        classifyButton.setText("Use this category");
                        classifyButton.setOnAction(e -> handleConfirm());
                        classifyButton.setDisable(false);
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        resultLabel.setText("Failed to category: " + ex.getMessage());
                        classifyButton.setDisable(false);
                    });
                    return null;
                });
    }

    /**
     * Sets the description field and triggers classification
     *
     * @param description the transaction description to classify
     */
    public void setDescription(String description) {
        if (description != null && !description.isEmpty()) {
            this.descriptionField.setText(description);
            // 自动触发分类
            handleClassify();
        }
    }

    /**
     * Handles confirmation of selected category
     */
    @FXML
    private void handleConfirm() {
        confirmClicked = true;
        closeWindow();
    }

    /**
     * Handles cancellation of classification
     */
    @FXML
    private void handleCancel() {
        confirmClicked = false;
        closeWindow();
    }

    /**
     * Closes the classification window
     */
    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Gets the AI-recommended classification result
     *
     * @return the recommended category
     */
    public String getClassificationResult() {
        return classificationResult;
    }

    /**
     * Checks if user confirmed the classification
     *
     * @return true if confirmed, false otherwise
     */
    public boolean isConfirmClicked() {
        return confirmClicked;
    }
}