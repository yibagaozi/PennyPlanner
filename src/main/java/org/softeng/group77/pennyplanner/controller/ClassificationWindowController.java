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

@Component
public class ClassificationWindowController {
    @FXML
    private TextField descriptionField;

    @FXML
    private Label resultLabel;

    @FXML
    private Button classifyButton;

    @FXML
    private Button cancelButton;

    private TransactionAnalysisService transactionAnalysisService;

    private String classificationResult;
    private boolean confirmClicked = false;

    @Autowired
    public void setTransactionAnalysisService(TransactionAnalysisService transactionAnalysisService) {
        this.transactionAnalysisService = transactionAnalysisService;
    }

    @FXML
    private void initialize() {
        resultLabel.setText("Type transaction description and click to classify");
    }

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
     * 设置描述字段内容
     */
    public void setDescription(String description) {
        if (description != null && !description.isEmpty()) {
            this.descriptionField.setText(description);
            // 自动触发分类
            handleClassify();
        }
    }

    @FXML
    private void handleConfirm() {
        confirmClicked = true;
        closeWindow();
    }

    @FXML
    private void handleCancel() {
        confirmClicked = false;
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    public String getClassificationResult() {
        return classificationResult;
    }

    public boolean isConfirmClicked() {
        return confirmClicked;
    }
}