package org.softeng.group77.pennyplanner.model;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Transaction {

    private final String id;

    @Setter
    private String description;

    @Setter
    private String category;

    @NotNull(message = "Amount cannot be null")
    @Digits(integer = 15, fraction = 4, message = "Amount must have up to 15 digits in total with up to 4 decimal places")
    @Setter
    private BigDecimal amount;

    @Setter
    private LocalDateTime transactionDateTime;

    @Setter
    private String userId;

    private final LocalDateTime createdAt;

    public Transaction(String description, String category, BigDecimal amount, LocalDateTime transactionDateTime, String userId) {
        this.id = UUID.randomUUID().toString();
        this.description = description;
        this.category = category;
        this.amount = amount;
        this.transactionDateTime = transactionDateTime;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }


}
