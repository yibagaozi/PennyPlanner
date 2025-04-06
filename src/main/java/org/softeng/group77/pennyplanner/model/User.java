package org.softeng.group77.pennyplanner.model;

import javafx.beans.property.StringProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class User {
    private final String id;
    private StringProperty username;
    private StringProperty email;
    private StringProperty phone;
    private String passwordHash;
    private final LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    public User(StringProperty username, String passwordHash, StringProperty email, StringProperty phone) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.phone = phone;
        this.createdAt = LocalDateTime.now();
    }

    public User(String id, StringProperty username, StringProperty email, StringProperty phone, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.passwordHash = null;
        this.createdAt = createdAt;
        this.lastLoginAt = LocalDateTime.now();
    }

}
