package org.softeng.group77.pennyplanner.dto;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.softeng.group77.pennyplanner.model.Transaction;

public class TransactionDetail {

    private final StringProperty id = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty category = new SimpleStringProperty();
    private final ObjectProperty<BigDecimal> amount = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> transactionDateTime = new SimpleObjectProperty<>();
    private final StringProperty userId = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();

    public TransactionDetail() {
    }

    public TransactionDetail(Transaction transaction) {
        setId(transaction.getId());
        setDescription(transaction.getDescription());
        setCategory(transaction.getCategory());
        setAmount(transaction.getAmount());
        setTransactionDateTime(transaction.getTransactionDateTime());
        setUserId(transaction.getUserId());
        setCreatedAt(transaction.getCreatedAt());
    }

    public Transaction toModel() {
        return new Transaction(
            getDescription(),
            getCategory(),
            getAmount(),
            getTransactionDateTime(),
            getUserId()
        );
    }

    public StringProperty idProperty() {
        return id;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public StringProperty categoryProperty() {
        return category;
    }

    public ObjectProperty<BigDecimal> amountProperty() {
        return amount;
    }

    public ObjectProperty<LocalDateTime> transactionDateTimeProperty() {
        return transactionDateTime;
    }

    public StringProperty userIdProperty() {
        return userId;
    }

    public ObjectProperty<LocalDateTime> createdAtProperty() {
        return createdAt;
    }

    public String getId() {
        return id.get();
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public String getCategory() {
        return category.get();
    }

    public void setCategory(String category) {
        this.category.set(category);
    }

    public BigDecimal getAmount() {
        return amount.get();
    }

    public void setAmount(BigDecimal amount) {
        this.amount.set(amount);
    }

    public LocalDateTime getTransactionDateTime() {
        return transactionDateTime.get();
    }

    public void setTransactionDateTime(LocalDateTime dateTime) {
        this.transactionDateTime.set(dateTime);
    }

    public String getUserId() {
        return userId.get();
    }

    public void setUserId(String userId) {
        this.userId.set(userId);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt.get();
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt.set(createdAt);
    }

    public boolean isExpense() {
        return amount.get() != null && amount.get().compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isIncome() {
        return amount.get() != null && amount.get().compareTo(BigDecimal.ZERO) > 0;
    }

    public String getFormattedAmount() {
        if (amount.get() == null) {
            return "0.00";
        }
        return (isIncome() ? "+" : "-") + amount.get().toString();
    }
}