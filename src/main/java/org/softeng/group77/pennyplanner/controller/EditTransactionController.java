package org.softeng.group77.pennyplanner.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.softeng.group77.pennyplanner.controller.tableModel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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

    /**
     * 初始化控制器
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
    }

    /**
     * 设置要编辑的交易记录
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
     * 处理"支出"单选按钮事件
     */
    @FXML
    private void handleExpense() {
        isExpense = true;
    }

    /**
     * 处理"收入"单选按钮事件
     */
    @FXML
    private void handleIncome() {
        isExpense = false;
    }

    /**
     * 处理"保存"按钮事件
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
     * 处理"取消"按钮事件
     */
    @FXML
    private void handleCancel() {
        saveClicked = false;
        closeDialog();
    }

    /**
     * 检查输入是否有效
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
     * 关闭对话框
     */
    private void closeDialog() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    /**
     * 返回是否点击了"保存"按钮
     */
    public boolean isSaveClicked() {
        return saveClicked;
    }
}