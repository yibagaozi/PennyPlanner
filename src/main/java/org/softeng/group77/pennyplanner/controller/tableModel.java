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
    
    private final StringProperty id;           // 交易ID，UUID形式
    private final StringProperty date;         // 交易日期
    private final StringProperty description;  // 交易描述
    private final DoubleProperty amount;       // 交易金额
    private final StringProperty category;     // 交易类别
    private final StringProperty method;       // 交易支付方式
    private final SimpleStringProperty displayId;  // 显示ID，用于展示在UI中的序号

    /**
     * 构造函数，用于初始化交易记录的各个属性。
     * 
     * @param id 交易ID
     * @param date 交易日期
     * @param description 交易描述
     * @param amount 交易金额
     * @param category 交易类别
     * @param method 交易支付方式
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

    /**
     * 获取交易日期。
     * 
     * @return 交易日期
     */
    public String getDate() { 
        return date.get(); 
    }

    /**
     * 获取交易描述。
     * 
     * @return 交易描述
     */
    public String getDescription() { 
        return description.get(); 
    }

    /**
     * 获取交易金额。
     * 
     * @return 交易金额
     */
    public double getAmount() { 
        return amount.get(); 
    }

    /**
     * 获取交易类别。
     * 
     * @return 交易类别
     */
    public String getCategory() { 
        return category.get(); 
    }

    /**
     * 获取交易支付方式。
     * 
     * @return 交易支付方式
     */
    public String getMethod() { 
        return method.get(); 
    }

    /**
     * 获取交易的显示ID。
     * 
     * @return 交易的显示ID
     */
    public String getDisplayId() { 
        return displayId.get(); 
    }

    /**
     * 设置交易日期。
     * 
     * @param value 交易日期
     */
    public void setDate(String value) { 
        date.set(value); 
    }

    /**
     * 设置交易描述。
     * 
     * @param value 交易描述
     */
    public void setDescription(String value) { 
        description.set(value); 
    }

    /**
     * 设置交易金额。
     * 
     * @param value 交易金额
     */
    public void setAmount(double value) { 
        amount.set(value); 
    }

    /**
     * 设置交易类别。
     * 
     * @param value 交易类别
     */
    public void setCategory(String value) { 
        category.set(value); 
    }

    /**
     * 设置交易支付方式。
     * 
     * @param value 交易支付方式
     */
    public void setMethod(String value) { 
        method.set(value); 
    }

    /**
     * 设置交易的显示ID。
     * 
     * @param value 交易显示ID
     */
    public void setDisplayId(String value) { 
        displayId.set(value); 
    }

    /**
     * 获取交易日期的属性（用于 TableView 绑定）。
     * 
     * @return 交易日期的属性
     */
    public StringProperty dateProperty() { 
        return date; 
    }

    /**
     * 获取交易描述的属性（用于 TableView 绑定）。
     * 
     * @return 交易描述的属性
     */
    public StringProperty descriptionProperty() { 
        return description; 
    }

    /**
     * 获取交易金额的属性（用于 TableView 绑定）。
     * 
     * @return 交易金额的属性
     */
    public DoubleProperty amountProperty() { 
        return amount; 
    }

    /**
     * 获取交易类别的属性（用于 TableView 绑定）。
     * 
     * @return 交易类别的属性
     */
    public StringProperty categoryProperty() { 
        return category; 
    }

    /**
     * 获取交易支付方式的属性（用于 TableView 绑定）。
     * 
     * @return 交易支付方式的属性
     */
    public StringProperty methodProperty() { 
        return method; 
    }

    /**
     * 获取交易显示ID的属性（用于 TableView 绑定）。
     * 
     * @return 交易显示ID的属性
     */
    public StringProperty displayIdProperty() { 
        return displayId; 
    }

    /**
     * 获取交易ID。
     * 
     * @return 交易ID
     */
    public String getId() {
        return id.get();
    }

    /**
     * 获取交易ID的属性（用于 TableView 绑定）。
     * 
     * @return 交易ID的属性
     */
    public StringProperty idProperty() {
        return id;
    }
}
