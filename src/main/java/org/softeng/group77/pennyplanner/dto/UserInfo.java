package org.softeng.group77.pennyplanner.dto;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.ObjectProperty;
import lombok.Getter;
import org.softeng.group77.pennyplanner.model.User;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * Data Transfer Object for user information with JavaFX property support.
 * This class wraps User model data for display in the UI and provides
 * JavaFX properties for binding to UI controls, enabling automatic updates
 * when user data changes.
 *
 * @author MA Ruize
 * @version 2.0.0
 * @since 1.0.0
 */
public class UserInfo {

    @Getter
    private final String id;

    private final StringProperty username = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty phone = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> lastLoginAt = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();

    public UserInfo() {
        id = "";
    }


    public UserInfo(String id) {
        this.id = id;
    }


    public UserInfo(User user) {
        this.id = user.getId();
        this.username.set(user.getUsername());
        this.email.set(user.getEmail());
        this.phone.set(user.getPhone());
        this.lastLoginAt.set(user.getLastLoginAt());
        this.createdAt.set(user.getCreatedAt());
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public String getUsername() {
        return username.get();
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public StringProperty emailProperty() {
        return email;
    }

    public String getEmail() {
        return email.get();
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public StringProperty phoneProperty() {
        return phone;
    }

    public String getPhone() {
        return phone.get();
    }

    public void setPhone(String phone) {
        this.phone.set(phone);
    }

    public ObjectProperty<LocalDateTime> lastLoginAtProperty() {
        return lastLoginAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt.get();
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt.set(lastLoginAt);
    }

    public ObjectProperty<LocalDateTime> createdAtProperty() {
        return createdAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt.get();
    }

}
