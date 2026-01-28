package com.inventory.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

/**
 * StockTransaction - Represents a stock movement transaction
 */
public class StockTransaction {

    private final IntegerProperty transactionId;
    private final IntegerProperty productId;
    private final StringProperty productCode;
    private final StringProperty productName;
    private final StringProperty transactionType;
    private final IntegerProperty quantity;
    private final ObjectProperty<LocalDateTime> transactionDate;
    private final StringProperty referenceNumber;
    private final StringProperty notes;
    private final StringProperty performedBy;

    public StockTransaction() {
        this.transactionId = new SimpleIntegerProperty();
        this.productId = new SimpleIntegerProperty();
        this.productCode = new SimpleStringProperty();
        this.productName = new SimpleStringProperty();
        this.transactionType = new SimpleStringProperty();
        this.quantity = new SimpleIntegerProperty();
        this.transactionDate = new SimpleObjectProperty<>();
        this.referenceNumber = new SimpleStringProperty();
        this.notes = new SimpleStringProperty();
        this.performedBy = new SimpleStringProperty();
    }

    // Getters and Setters
    public int getTransactionId() {
        return transactionId.get();
    }

    public void setTransactionId(int value) {
        transactionId.set(value);
    }

    public IntegerProperty transactionIdProperty() {
        return transactionId;
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

    public String getTransactionType() {
        return transactionType.get();
    }

    public void setTransactionType(String value) {
        transactionType.set(value);
    }

    public StringProperty transactionTypeProperty() {
        return transactionType;
    }

    public int getQuantity() {
        return quantity.get();
    }

    public void setQuantity(int value) {
        quantity.set(value);
    }

    public IntegerProperty quantityProperty() {
        return quantity;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate.get();
    }

    public void setTransactionDate(LocalDateTime value) {
        transactionDate.set(value);
    }

    public ObjectProperty<LocalDateTime> transactionDateProperty() {
        return transactionDate;
    }

    public String getReferenceNumber() {
        return referenceNumber.get();
    }

    public void setReferenceNumber(String value) {
        referenceNumber.set(value);
    }

    public StringProperty referenceNumberProperty() {
        return referenceNumber;
    }

    public String getNotes() {
        return notes.get();
    }

    public void setNotes(String value) {
        notes.set(value);
    }

    public StringProperty notesProperty() {
        return notes;
    }

    public String getPerformedBy() {
        return performedBy.get();
    }

    public void setPerformedBy(String value) {
        performedBy.set(value);
    }

    public StringProperty performedByProperty() {
        return performedBy;
    }

    // Additional properties for service layer
    private final DoubleProperty unitPrice = new SimpleDoubleProperty();
    private final DoubleProperty totalPrice = new SimpleDoubleProperty();

    public double getUnitPrice() {
        return unitPrice.get();
    }

    public void setUnitPrice(double value) {
        unitPrice.set(value);
    }

    public DoubleProperty unitPriceProperty() {
        return unitPrice;
    }

    public double getTotalPrice() {
        return totalPrice.get();
    }

    public void setTotalPrice(double value) {
        totalPrice.set(value);
    }

    public DoubleProperty totalPriceProperty() {
        return totalPrice;
    }

    // ========== Adapter Methods for Service Layer Compatibility ==========

    /**
     * Adapter: setUsername() -> setPerformedBy()
     */
    public void setUsername(String username) {
        setPerformedBy(username);
    }

    /**
     * Adapter: getUsername() -> getPerformedBy()
     */
    public String getUsername() {
        return getPerformedBy();
    }

    /**
     * Adapter: getId() -> getTransactionId()
     */
    public int getId() {
        return getTransactionId();
    }

    /**
     * Adapter: setId() -> setTransactionId()
     */
    public void setId(int id) {
        setTransactionId(id);
    }
}
