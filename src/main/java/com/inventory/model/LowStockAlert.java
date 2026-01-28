package com.inventory.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

/**
 * LowStockAlert - Represents a low stock alert
 */
public class LowStockAlert {

    private final IntegerProperty alertId;
    private final IntegerProperty productId;
    private final StringProperty productCode;
    private final StringProperty productName;
    private final ObjectProperty<LocalDateTime> alertDate;
    private final IntegerProperty currentStock;
    private final IntegerProperty reorderLevel;
    private final StringProperty status;
    private final ObjectProperty<LocalDateTime> acknowledgedAt;
    private final StringProperty acknowledgedBy;

    public LowStockAlert() {
        this.alertId = new SimpleIntegerProperty();
        this.productId = new SimpleIntegerProperty();
        this.productCode = new SimpleStringProperty();
        this.productName = new SimpleStringProperty();
        this.alertDate = new SimpleObjectProperty<>();
        this.currentStock = new SimpleIntegerProperty();
        this.reorderLevel = new SimpleIntegerProperty();
        this.status = new SimpleStringProperty("pending");
        this.acknowledgedAt = new SimpleObjectProperty<>();
        this.acknowledgedBy = new SimpleStringProperty();
    }

    // Getters and Setters
    public int getAlertId() {
        return alertId.get();
    }

    public void setAlertId(int value) {
        alertId.set(value);
    }

    public IntegerProperty alertIdProperty() {
        return alertId;
    }

    public int getProductId() {
        return productId.get();
    }

    public void setProductId(int value) {
        productId.set(value);
    }

    public IntegerProperty productIdProperty() {
        return productId;
    }

    public String getProductCode() {
        return productCode.get();
    }

    public void setProductCode(String value) {
        productCode.set(value);
    }

    public StringProperty productCodeProperty() {
        return productCode;
    }

    public String getProductName() {
        return productName.get();
    }

    public void setProductName(String value) {
        productName.set(value);
    }

    public StringProperty productNameProperty() {
        return productName;
    }

    public LocalDateTime getAlertDate() {
        return alertDate.get();
    }

    public void setAlertDate(LocalDateTime value) {
        alertDate.set(value);
    }

    public ObjectProperty<LocalDateTime> alertDateProperty() {
        return alertDate;
    }

    public int getCurrentStock() {
        return currentStock.get();
    }

    public void setCurrentStock(int value) {
        currentStock.set(value);
    }

    public IntegerProperty currentStockProperty() {
        return currentStock;
    }

    public int getReorderLevel() {
        return reorderLevel.get();
    }

    public void setReorderLevel(int value) {
        reorderLevel.set(value);
    }

    public IntegerProperty reorderLevelProperty() {
        return reorderLevel;
    }

    public String getStatus() {
        return status.get();
    }

    public void setStatus(String value) {
        status.set(value);
    }

    public StringProperty statusProperty() {
        return status;
    }

    public LocalDateTime getAcknowledgedAt() {
        return acknowledgedAt.get();
    }

    public void setAcknowledgedAt(LocalDateTime value) {
        acknowledgedAt.set(value);
    }

    public ObjectProperty<LocalDateTime> acknowledgedAtProperty() {
        return acknowledgedAt;
    }

    public String getAcknowledgedBy() {
        return acknowledgedBy.get();
    }

    public void setAcknowledgedBy(String value) {
        acknowledgedBy.set(value);
    }

    public StringProperty acknowledgedByProperty() {
        return acknowledgedBy;
    }

    /**
     * Get shortage quantity (how many units below reorder level)
     */
    public int getShortageQuantity() {
        return Math.max(0, getReorderLevel() - getCurrentStock());
    }

    // Additional properties for service layer
    private final BooleanProperty resolved = new SimpleBooleanProperty(false);
    private final ObjectProperty<LocalDateTime> resolvedDate = new SimpleObjectProperty<>();

    public boolean isResolved() {
        return resolved.get();
    }

    public void setResolved(boolean value) {
        resolved.set(value);
    }

    public BooleanProperty resolvedProperty() {
        return resolved;
    }

    public LocalDateTime getResolvedDate() {
        return resolvedDate.get();
    }

    public void setResolvedDate(LocalDateTime value) {
        resolvedDate.set(value);
    }

    public ObjectProperty<LocalDateTime> resolvedDateProperty() {
        return resolvedDate;
    }

    // ========== Adapter Methods for Service Layer Compatibility ==========

    /**
     * Adapter: setMinStockLevel() -> setReorderLevel()
     */
    public void setMinStockLevel(int level) {
        setReorderLevel(level);
    }

    /**
     * Adapter: getMinStockLevel() -> getReorderLevel()
     */
    public int getMinStockLevel() {
        return getReorderLevel();
    }

    /**
     * Adapter: getId() -> getAlertId()
     */
    public int getId() {
        return getAlertId();
    }

    /**
     * Adapter: setId() -> setAlertId()
     */
    public void setId(int id) {
        setAlertId(id);
    }
}
