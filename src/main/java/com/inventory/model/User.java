package com.inventory.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

/**
 * User - Represents a system user with authentication
 */
public class User {

    private final IntegerProperty userId;
    private final StringProperty username;
    private final StringProperty passwordHash;
    private final StringProperty fullName;
    private final StringProperty email;
    private final StringProperty role;
    private final BooleanProperty isActive;
    private final ObjectProperty<LocalDateTime> lastLogin;
    private final ObjectProperty<LocalDateTime> createdAt;
    private final ObjectProperty<LocalDateTime> updatedAt;

    public User() {
        this.userId = new SimpleIntegerProperty();
        this.username = new SimpleStringProperty();
        this.passwordHash = new SimpleStringProperty();
        this.fullName = new SimpleStringProperty();
        this.email = new SimpleStringProperty();
        this.role = new SimpleStringProperty();
        this.isActive = new SimpleBooleanProperty(true);
        this.lastLogin = new SimpleObjectProperty<>();
        this.createdAt = new SimpleObjectProperty<>();
        this.updatedAt = new SimpleObjectProperty<>();
    }

    // User ID
    public int getUserId() {
        return userId.get();
    }

    public void setUserId(int value) {
        userId.set(value);
    }

    public IntegerProperty userIdProperty() {
        return userId;
    }

    // Username
    public String getUsername() {
        return username.get();
    }

    public void setUsername(String value) {
        username.set(value);
    }

    public StringProperty usernameProperty() {
        return username;
    }

    // Password Hash
    public String getPasswordHash() {
        return passwordHash.get();
    }

    public void setPasswordHash(String value) {
        passwordHash.set(value);
    }

    public StringProperty passwordHashProperty() {
        return passwordHash;
    }

    // Full Name
    public String getFullName() {
        return fullName.get();
    }

    public void setFullName(String value) {
        fullName.set(value);
    }

    public StringProperty fullNameProperty() {
        return fullName;
    }

    // Email
    public String getEmail() {
        return email.get();
    }

    public void setEmail(String value) {
        email.set(value);
    }

    public StringProperty emailProperty() {
        return email;
    }

    // Role
    public String getRole() {
        return role.get();
    }

    public void setRole(String value) {
        role.set(value);
    }

    public StringProperty roleProperty() {
        return role;
    }

    // Is Active
    public boolean isActive() {
        return isActive.get();
    }

    public void setActive(boolean value) {
        isActive.set(value);
    }

    public BooleanProperty isActiveProperty() {
        return isActive;
    }

    // Last Login
    public LocalDateTime getLastLogin() {
        return lastLogin.get();
    }

    public void setLastLogin(LocalDateTime value) {
        lastLogin.set(value);
    }

    public ObjectProperty<LocalDateTime> lastLoginProperty() {
        return lastLogin;
    }

    // Created At
    public LocalDateTime getCreatedAt() {
        return createdAt.get();
    }

    public void setCreatedAt(LocalDateTime value) {
        createdAt.set(value);
    }

    public ObjectProperty<LocalDateTime> createdAtProperty() {
        return createdAt;
    }

    // Updated At
    public LocalDateTime getUpdatedAt() {
        return updatedAt.get();
    }

    public void setUpdatedAt(LocalDateTime value) {
        updatedAt.set(value);
    }

    public ObjectProperty<LocalDateTime> updatedAtProperty() {
        return updatedAt;
    }

    // Helper methods
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(getRole());
    }

    public boolean isManager() {
        return "MANAGER".equalsIgnoreCase(getRole());
    }

    public boolean isStaff() {
        return "STAFF".equalsIgnoreCase(getRole());
    }

    @Override
    public String toString() {
        return String.format("User[id=%d, username=%s, fullName=%s, role=%s]",
                getUserId(), getUsername(), getFullName(), getRole());
    }
}