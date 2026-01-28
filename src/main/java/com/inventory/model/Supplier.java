package com.inventory.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

/**
 * Supplier - Represents a product supplier
 */
public class Supplier {

    private final IntegerProperty supplierId;
    private final StringProperty supplierName;
    private final StringProperty contactPerson;
    private final StringProperty email;
    private final StringProperty phone;
    private final StringProperty address;
    private final ObjectProperty<LocalDateTime> createdAt;
    private final ObjectProperty<LocalDateTime> updatedAt;

    public Supplier() {
        this.supplierId = new SimpleIntegerProperty();
        this.supplierName = new SimpleStringProperty();
        this.contactPerson = new SimpleStringProperty();
        this.email = new SimpleStringProperty();
        this.phone = new SimpleStringProperty();
        this.address = new SimpleStringProperty();
        this.createdAt = new SimpleObjectProperty<>();
        this.updatedAt = new SimpleObjectProperty<>();
    }

    public Supplier(String supplierName, String contactPerson, String email, String phone) {
        this();
        setSupplierName(supplierName);
        setContactPerson(contactPerson);
        setEmail(email);
        setPhone(phone);
    }

    // Getters and Setters
    public int getSupplierId() {
        return supplierId.get();
    }

    public void setSupplierId(int value) {
        supplierId.set(value);
    }

    public IntegerProperty supplierIdProperty() {
        return supplierId;
    }

    public String getSupplierName() {
        return supplierName.get();
    }

    public void setSupplierName(String value) {
        supplierName.set(value);
    }

    public StringProperty supplierNameProperty() {
        return supplierName;
    }

    public String getContactPerson() {
        return contactPerson.get();
    }

    public void setContactPerson(String value) {
        contactPerson.set(value);
    }

    public StringProperty contactPersonProperty() {
        return contactPerson;
    }

    public String getEmail() {
        return email.get();
    }

    public void setEmail(String value) {
        email.set(value);
    }

    public StringProperty emailProperty() {
        return email;
    }

    public String getPhone() {
        return phone.get();
    }

    public void setPhone(String value) {
        phone.set(value);
    }

    public StringProperty phoneProperty() {
        return phone;
    }

    public String getAddress() {
        return address.get();
    }

    public void setAddress(String value) {
        address.set(value);
    }

    public StringProperty addressProperty() {
        return address;
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
     * Adapter: getName() -> getSupplierName()
     */
    public String getName() {
        return getSupplierName();
    }

    /**
     * Adapter: setName() -> setSupplierName()
     */
    public void setName(String name) {
        setSupplierName(name);
    }

    /**
     * Adapter: getId() -> getSupplierId()
     */
    public int getId() {
        return getSupplierId();
    }

    /**
     * Adapter: setId() -> setSupplierId()
     */
    public void setId(int id) {
        setSupplierId(id);
    }

    @Override
    public String toString() {
        return getSupplierName();
    }
}
