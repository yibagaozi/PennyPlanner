package org.softeng.group77.pennyplanner.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.softeng.group77.pennyplanner.controller.tableModel;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

/**
 * Controller for the transaction editing view in the PennyPlanner application.
 * Handles editing existing transaction details including type (income/expense),
 * amount, date, description, category, and payment method.
 *
 * @author CHAI Jiayang
 * @author WANG Bingsong
 * @version 2.0.0
 * @since 1.2.0
 */
@Controller
public class EditTransactionController {
    @FXML private RadioButton expenseRadioButton;
    @FXML private RadioButton incomeRadioButton;
    @FXML private DatePicker datePicker;
    @FXML private TextField descriptionField;
    @FXML private TextField amountField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> methodComboBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private tableModel transaction;
    private boolean isExpense = true;
    private boolean saveClicked = false;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML private Button suggestCategoryButton;
    private ApplicationContext applicationContext;

    /**
     * Sets the Spring application context
     *
     * @param applicationContext the Spring application context
     */
    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Initializes the controller after FXML elements are loaded
     */
    @FXML
    private void initialize() {
        // 初始化分类选择
        categoryComboBox.setItems(FXCollections.observableArrayList(
                "Food", "Salary", "Living Bill", "Entertainment",
                "Transportation", "Education", "Clothes", "Others"
        ));

        // 初始化支付方式
        methodComboBox.setItems(FXCollections.observableArrayList(
                "Credit Card", "Bank Transfer", "Auto-Payment", "Cash", "E-Payment"
        ));

        if(suggestCategoryButton != null) {
            suggestCategoryButton.setOnAction(e -> handleSuggestCategory());
        }
    }

    /**
     * Sets the transaction to be edited and populates form fields
     *
     * @param transaction the transaction model to edit
     */
    public void setTransaction(tableModel transaction) {
        this.transaction = transaction;

        // 设置初始值
        try {
            LocalDate date = LocalDate.parse(transaction.getDate(), DATE_FORMATTER);
            datePicker.setValue(date);
        } catch (DateTimeParseException e) {
            datePicker.setValue(LocalDate.now());
        }

        descriptionField.setText(transaction.getDescription());

        // 设置金额并确定是收入还是支出
        double amount = transaction.getAmount();
        if (amount < 0) {
            isExpense = true;
            expenseRadioButton.setSelected(true);
            amountField.setText(String.format("%.2f", Math.abs(amount)));
        } else {
            isExpense = false;
            incomeRadioButton.setSelected(true);
            amountField.setText(String.format("%.2f", amount));
        }

        categoryComboBox.setValue(transaction.getCategory());
        methodComboBox.setValue(transaction.getMethod());
    }

    /**
     * Handles the "Expense" radio button selection event
     */
    @FXML
    private void handleExpense() {
        isExpense = true;
    }

    /**
     * Handles the "Income" radio button selection event
     */
    @FXML
    private void handleIncome() {
        isExpense = false;
    }

    /**
     * Handles the "Save" button click event
     * Validates input and updates the transaction model
     */
    @FXML
    private void handleSave() {
        if (isInputValid()) {
            // 更新交易记录对象
            transaction.setDate(datePicker.getValue().format(DATE_FORMATTER));
            transaction.setDescription(descriptionField.getText());

            double amount = Double.parseDouble(amountField.getText());
            if (isExpense) {
                amount = -Math.abs(amount);  // 确保支出为负数
            } else {
                amount = Math.abs(amount);  // 确保收入为正数
            }
            transaction.setAmount(amount);

            transaction.setCategory(categoryComboBox.getValue());
            transaction.setMethod(methodComboBox.getValue());

            saveClicked = true;
            closeDialog();
        }
    }

    /**
     * Handles the "Cancel" button click event
     */
    @FXML
    private void handleCancel() {
        saveClicked = false;
        closeDialog();
    }

    /**
     * Validates user input before saving
     *
     * @return true if all inputs are valid, false otherwise
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (datePicker.getValue() == null) {
            errorMessage += "日期不能为空!\n";
        }

        if (descriptionField.getText() == null || descriptionField.getText().isEmpty()) {
            errorMessage += "描述不能为空!\n";
        }

        if (amountField.getText() == null || amountField.getText().isEmpty()) {
            errorMessage += "金额不能为空!\n";
        } else {
            try {
                double amount = Double.parseDouble(amountField.getText());
                if (amount <= 0) {
                    errorMessage += "金额必须大于0!\n";
                }
            } catch (NumberFormatException e) {
                errorMessage += "金额必须是有效数字!\n";
            }
        }

        if (categoryComboBox.getValue() == null || categoryComboBox.getValue().isEmpty()) {
            errorMessage += "类别不能为空!\n";
        }

        if (methodComboBox.getValue() == null || methodComboBox.getValue().isEmpty()) {
            errorMessage += "支付方式不能为空!\n";
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            // 显示错误信息
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("输入错误");
            alert.setHeaderText("请修正以下错误:");
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return false;
        }
    }

    /**
     * Closes the dialog window
     */
    private void closeDialog() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Returns whether the Save button was clicked
     *
     * @return true if Save was clicked, false otherwise
     */
    public boolean isSaveClicked() {
        return saveClicked;
    }

    /**
     * Gets the current description text
     *
     * @return the description text
     */
    public String getDescriptionText() {
        return descriptionField.getText();
    }

    /**
     * Sets a category from AI suggestion
     *
     * @param category the suggested category
     */
    public void setCategoryFromAI(String category) {
        if (category != null && !category.isEmpty()) {
            categoryComboBox.setValue(category);
        }
    }

    /**
     * Handles the suggest category button click
     * Opens the AI classification window if description is provided
     */
    @FXML
    private void handleSuggestCategory() {
        String description = descriptionField.getText();
        if (description == null || description.trim().isEmpty()) {
            // 显示错误提示
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("输入不完整");
            alert.setHeaderText(null);
            alert.setContentText("请先输入交易描述");
            alert.showAndWait();
            return;
        }

        openClassificationWindow(description);
    }

    /**
     * Opens the classification window with the provided description
     *
     * @param description the transaction description to classify
     */
    private void openClassificationWindow(String description) {
        try {
            // 创建FXML加载器
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/classification_window_view.fxml"));
            loader.setControllerFactory(applicationContext::getBean);

            // 加载布局
            Scene scene = new Scene(loader.load());

            // 设置窗口
            Stage dialogStage = new Stage();
            dialogStage.setTitle("AI-Classification");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(scene);

            // 获取控制器并设置描述
            ClassificationWindowController controller = loader.getController();
            controller.setDescription(description);

            // 显示窗口并等待关闭
            dialogStage.showAndWait();

            // 如果用户确认使用分类结果，则更新分类字段
            if (controller.isConfirmClicked()) {
                String category = controller.getClassificationResult();
                setCategoryFromAI(category);
            }

        } catch (IOException e) {
            e.printStackTrace();
            // 显示错误对话框
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("错误");
            alert.setHeaderText(null);
            alert.setContentText("无法打开分类窗口: " + e.getMessage());
            alert.showAndWait();
        }
    }
    private void openClassificationWindow() {
        String description = descriptionField.getText();
        if (description == null || description.trim().isEmpty()) {
            // 显示错误提示
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("输入不完整");
            alert.setHeaderText(null);
            alert.setContentText("请先输入交易描述");
            alert.showAndWait();
            return;
        }

        openClassificationWindow(description);
    }
}