package org.softeng.group77.pennyplanner.controller;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * tableModel 用于表示一条交易记录的实体类，包含了交易的各个属性（如日期、描述、金额、类别、支付方式等）。
 * 该类与 TableView 控件绑定，使得用户界面能够显示和操作交易记录的数据。
 */
public class tableModel {
    private final StringProperty id;
    private final StringProperty date;       // 对应Date列
    private final StringProperty description; // 对应Description列
    private final DoubleProperty amount;     // 对应Amount列
    private final StringProperty category;   // 对应Category列
    private final StringProperty method;     // 对应Method列
    private final SimpleStringProperty displayId;

    // 构造函数
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
