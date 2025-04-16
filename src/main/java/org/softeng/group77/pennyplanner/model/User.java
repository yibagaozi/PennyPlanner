package org.softeng.group77.pennyplanner.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class User {

    private final String id;

    @Setter
    private String username;

    @Setter
    private String email;

    @Setter
    private String phone;

    @Setter
    private String passwordHash;

    private final LocalDateTime createdAt;

    @Setter
    private LocalDateTime lastLoginAt;

    public User (){
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }

    public User(String username, String passwordHash, String email, String phone) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.createdAt = LocalDateTime.now();
    }

    public User(String id, String username, String email, String phone, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.passwordHash = null;
        this.createdAt = createdAt;
        this.lastLoginAt = LocalDateTime.now();
    }

}
