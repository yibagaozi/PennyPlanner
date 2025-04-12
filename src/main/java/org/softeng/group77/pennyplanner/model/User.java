package org.softeng.group77.pennyplanner.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

public class User {
    @Getter
    private final String id;
    private StringProperty username;
    private StringProperty email;
    private StringProperty phone;
    @Getter
    @Setter
    private String passwordHash;
    @Getter
    private final LocalDateTime createdAt;
    @Getter
    @Setter
    private LocalDateTime lastLoginAt;

    public User(String username, String passwordHash, String email, String phone) {
        this.id = UUID.randomUUID().toString();
        this.username = new SimpleStringProperty(username);
        this.email = new SimpleStringProperty(email);
        this.phone = new SimpleStringProperty(phone);
        this.passwordHash = passwordHash;
        this.createdAt = LocalDateTime.now();
    }

    public User(String id, StringProperty username, StringProperty email, StringProperty phone, LocalDateTime createdAt) {
        this.id = id;
        this.username = username != null ? username : new SimpleStringProperty();
        this.email = email != null ? email : new SimpleStringProperty();
        this.phone = phone != null ? phone : new SimpleStringProperty();
        this.passwordHash = null;
        this.createdAt = createdAt;
        this.lastLoginAt = LocalDateTime.now();
    }

    public String getUsername() {
        return username.get();
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public String getEmail() {
        return email.get();
    }

    public StringProperty emailProperty() {
        return email;
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public String getPhone() {
        return phone.get();
    }

    public StringProperty phoneProperty() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone.set(phone);
    }

}
