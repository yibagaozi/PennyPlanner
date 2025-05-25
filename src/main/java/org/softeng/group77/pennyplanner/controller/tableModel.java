package org.softeng.group77.pennyplanner.controller;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Transaction model class for the PennyPlanner application.
 * This class represents a financial transaction with properties for date,
 * description, amount, category, and payment method. It uses JavaFX property
 * binding to support direct integration with TableView controls in the UI.
 *
 * @author CHAI Jiayang
 * @version 2.0.0
 * @since 1.0.0
 */
public class tableModel {
    private final StringProperty id;
    private final StringProperty date;       // 对应Date列
    private final StringProperty description; // 对应Description列
    private final DoubleProperty amount;     // 对应Amount列
    private final StringProperty category;   // 对应Category列
    private final StringProperty method;     // 对应Method列
    private final SimpleStringProperty displayId;

    /**
     * Creates a new transaction model with the specified properties
     *
     * @param id the unique identifier for the transaction
     * @param date the transaction date in string format
     * @param description the transaction description
     * @param amount the transaction amount (positive for income, negative for expense)
     * @param category the transaction category
     * @param method the payment method used
     */
    public tableModel(String id, String date, String description, double amount,
                      String category, String method) {
        this.id = new SimpleStringProperty(id); // 保留原始UUID
        this.displayId = new SimpleStringProperty(""); // 初始为空，由适配器设置序号
        this.date = new SimpleStringProperty(date);
        this.description = new SimpleStringProperty(description);
        this.amount = new SimpleDoubleProperty(amount);
        this.category = new SimpleStringProperty(category);
        this.method = new SimpleStringProperty(method);
    }

    // Getter方法
    public String getDate() { return date.get(); }
    public String getDescription() { return description.get(); }
    public double getAmount() { return amount.get(); }
    public String getCategory() { return category.get(); }
    public String getMethod() { return method.get(); }
    public String getDisplayId() { return displayId.get(); }

    // Setter方法
    public void setDate(String value) { date.set(value); }
    public void setDescription(String value) { description.set(value); }
    public void setAmount(double value) { amount.set(value); }
    public void setCategory(String value) { category.set(value); }
    public void setMethod(String value) { method.set(value); }
    public void setDisplayId(String value) { displayId.set(value); }

    // Property getter方法(用于TableView绑定)
    public StringProperty dateProperty() { return date; }
    public StringProperty descriptionProperty() { return description; }
    public DoubleProperty amountProperty() { return amount; }
    public StringProperty categoryProperty() { return category; }
    public StringProperty methodProperty() { return method; }
    public StringProperty displayIdProperty() { return displayId; }

    public String getId() {
        return id.get();
    }

    public StringProperty idProperty() {
        return id;
    }
}
