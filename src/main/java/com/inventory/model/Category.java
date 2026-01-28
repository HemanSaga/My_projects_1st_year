package com.inventory.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

/**
 * Category - Represents a product category
 */
public class Category {

    private final IntegerProperty categoryId;
    private final StringProperty categoryName;
    private final StringProperty description;
    private final ObjectProperty<LocalDateTime> createdAt;
    private final ObjectProperty<LocalDateTime> updatedAt;

    public Category() {
        this.categoryId = new SimpleIntegerProperty();
        this.categoryName = new SimpleStringProperty();
        this.description = new SimpleStringProperty();
        this.createdAt = new SimpleObjectProperty<>();
        this.updatedAt = new SimpleObjectProperty<>();
    }

    public Category(String categoryName, String description) {
        this();
        setCategoryName(categoryName);
        setDescription(description);
    }

    // Getters and Setters
    public int getCategoryId() {
        return categoryId.get();
    }

    public void setCategoryId(int value) {
        categoryId.set(value);
    }

    public IntegerProperty categoryIdProperty() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName.get();
    }

    public void setCategoryName(String value) {
        categoryName.set(value);
    }

    public StringProperty categoryNameProperty() {
        return categoryName;
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String value) {
        description.set(value);
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt.get();
    }

    public void setCreatedAt(LocalDateTime value) {
        createdAt.set(value);
    }

    public ObjectProperty<LocalDateTime> createdAtProperty() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt.get();
    }

    public void setUpdatedAt(LocalDateTime value) {
        updatedAt.set(value);
    }

    public ObjectProperty<LocalDateTime> updatedAtProperty() {
        return updatedAt;
    }

    // ========== Adapter Methods for Service Layer Compatibility ==========

    /**
     * Adapter: getName() -> getCategoryName()
     */
    public String getName() {
        return getCategoryName();
    }

    /**
     * Adapter: setName() -> setCategoryName()
     */
    public void setName(String name) {
        setCategoryName(name);
    }

    /**
     * Adapter: getId() -> getCategoryId()
     */
    public int getId() {
        return getCategoryId();
    }

    /**
     * Adapter: setId() -> setCategoryId()
     */
    public void setId(int id) {
        setCategoryId(id);
    }

    @Override
    public String toString() {
        return getCategoryName();
    }
}
