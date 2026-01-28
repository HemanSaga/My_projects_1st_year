// ============================================
// FILE: src/main/java/com/inventory/model/Product.java
// Product entity model class
// ============================================

package com.inventory.model;

import javafx.beans.property.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product - Represents a product in the inventory system
 * Uses JavaFX properties for automatic UI binding
 */
public class Product {

    // Properties for JavaFX TableView binding
    private final IntegerProperty productId;
    private final StringProperty productCode;
    private final StringProperty productName;
    private final StringProperty description;
    private final IntegerProperty categoryId;
    private final StringProperty categoryName;
    private final IntegerProperty supplierId;
    private final StringProperty supplierName;
    private final ObjectProperty<BigDecimal> unitPrice;
    private final IntegerProperty quantityInStock;
    private final IntegerProperty reorderLevel;
    private final StringProperty unitOfMeasure;
    private final StringProperty status;
    private final StringProperty barcode;
    private final StringProperty imageUrl;
    private final ObjectProperty<LocalDateTime> createdAt;
    private final ObjectProperty<LocalDateTime> updatedAt;

    /**
     * Default constructor - initializes all properties
     */
    public Product() {
        this.productId = new SimpleIntegerProperty();
        this.productCode = new SimpleStringProperty();
        this.productName = new SimpleStringProperty();
        this.description = new SimpleStringProperty();
        this.categoryId = new SimpleIntegerProperty();
        this.categoryName = new SimpleStringProperty();
        this.supplierId = new SimpleIntegerProperty();
        this.supplierName = new SimpleStringProperty();
        this.unitPrice = new SimpleObjectProperty<>(BigDecimal.ZERO);
        this.quantityInStock = new SimpleIntegerProperty();
        this.reorderLevel = new SimpleIntegerProperty();
        this.unitOfMeasure = new SimpleStringProperty();
        this.status = new SimpleStringProperty("active");
        this.barcode = new SimpleStringProperty();
        this.imageUrl = new SimpleStringProperty();
        this.createdAt = new SimpleObjectProperty<>();
        this.updatedAt = new SimpleObjectProperty<>();
    }

    /**
     * Constructor with essential fields
     */
    public Product(String productCode, String productName, BigDecimal unitPrice,
            int quantityInStock, int reorderLevel) {
        this();
        setProductCode(productCode);
        setProductName(productName);
        setUnitPrice(unitPrice);
        setQuantityInStock(quantityInStock);
        setReorderLevel(reorderLevel);
    }

    // ========== Getters and Setters ==========

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

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String value) {
        description.set(value);
    }

    public StringProperty descriptionProperty() {
        return description;
    }

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

    public BigDecimal getUnitPrice() {
        return unitPrice.get();
    }

    public void setUnitPrice(BigDecimal value) {
        unitPrice.set(value);
    }

    public ObjectProperty<BigDecimal> unitPriceProperty() {
        return unitPrice;
    }

    public int getQuantityInStock() {
        return quantityInStock.get();
    }

    public void setQuantityInStock(int value) {
        quantityInStock.set(value);
    }

    public IntegerProperty quantityInStockProperty() {
        return quantityInStock;
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

    public String getUnitOfMeasure() {
        return unitOfMeasure.get();
    }

    public void setUnitOfMeasure(String value) {
        unitOfMeasure.set(value);
    }

    public StringProperty unitOfMeasureProperty() {
        return unitOfMeasure;
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

    public String getBarcode() {
        return barcode.get();
    }

    public void setBarcode(String value) {
        barcode.set(value);
    }

    public StringProperty barcodeProperty() {
        return barcode;
    }

    public String getImageUrl() {
        return imageUrl.get();
    }

    public void setImageUrl(String value) {
        imageUrl.set(value);
    }

    public StringProperty imageUrlProperty() {
        return imageUrl;
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

    // ========== Helper Methods ==========

    /**
     * Check if product is low on stock
     */
    public boolean isLowStock() {
        return getQuantityInStock() < getReorderLevel();
    }

    /**
     * Check if product is out of stock
     */
    public boolean isOutOfStock() {
        return getQuantityInStock() == 0;
    }

    /**
     * Get total value of stock (price * quantity)
     */
    public BigDecimal getTotalValue() {
        return getUnitPrice().multiply(new BigDecimal(getQuantityInStock()));
    }

    /**
     * Get stock status as string
     */
    public String getStockStatus() {
        if (isOutOfStock()) {
            return "Out of Stock";
        } else if (isLowStock()) {
            return "Low Stock";
        } else {
            return "In Stock";
        }
    }

    // ========== Adapter Methods for Service Layer Compatibility ==========

    /**
     * Adapter method: getName() -> getProductName()
     */
    public String getName() {
        return getProductName();
    }

    /**
     * Adapter method: setName() -> setProductName()
     */
    public void setName(String name) {
        setProductName(name);
    }

    /**
     * Adapter method: getSku() -> getProductCode()
     */
    public String getSku() {
        return getProductCode();
    }

    /**
     * Adapter method: setSku() -> setProductCode()
     */
    public void setSku(String sku) {
        setProductCode(sku);
    }

    /**
     * Adapter method: getQuantity() -> getQuantityInStock()
     */
    public int getQuantity() {
        return getQuantityInStock();
    }

    /**
     * Adapter method: setQuantity() -> setQuantityInStock()
     */
    public void setQuantity(int quantity) {
        setQuantityInStock(quantity);
    }

    /**
     * Adapter method: getPrice() -> getUnitPrice() as double
     */
    public double getPrice() {
        return getUnitPrice() != null ? getUnitPrice().doubleValue() : 0.0;
    }

    /**
     * Adapter method: setPrice() -> setUnitPrice()
     */
    public void setPrice(double price) {
        setUnitPrice(new BigDecimal(price));
    }

    /**
     * Adapter method: getId() -> getProductId()
     */
    public int getId() {
        return getProductId();
    }

    /**
     * Adapter method: setId() -> setProductId()
     */
    public void setId(int id) {
        setProductId(id);
    }

    /**
     * Adapter method: getMinStockLevel() -> getReorderLevel()
     */
    public int getMinStockLevel() {
        return getReorderLevel();
    }

    /**
     * Adapter method: setMinStockLevel() -> setReorderLevel()
     */
    public void setMinStockLevel(int level) {
        setReorderLevel(level);
    }

    @Override
    public String toString() {
        return String.format("Product[id=%d, code=%s, name=%s, stock=%d]",
                getProductId(), getProductCode(), getProductName(), getQuantityInStock());
    }
}
