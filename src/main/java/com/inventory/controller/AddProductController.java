package com.inventory.controller;

import com.inventory.dao.CategoryDAO;
import com.inventory.dao.SupplierDAO;
import com.inventory.model.Category;
import com.inventory.model.Product;
import com.inventory.model.Supplier;
import com.inventory.service.ProductService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

public class AddProductController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(AddProductController.class);

    @FXML
    private TextField nameField;
    @FXML
    private TextField skuField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private ComboBox<Category> categoryCombo;
    @FXML
    private ComboBox<Supplier> supplierCombo;
    @FXML
    private TextField quantityField;
    @FXML
    private TextField priceField;
    @FXML
    private TextField minStockField;
    @FXML
    private Label errorLabel;

    private final ProductService productService = new ProductService();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();

    private boolean saved = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initializing AddProductController");
        loadCategories();
        loadSuppliers();
        setupValidation();

        // Set default values
        minStockField.setText("10");
        quantityField.setText("0");
        priceField.setText("0.00");
    }

    private void loadCategories() {
        try {
            List<Category> categories = categoryDAO.getAll();
            categoryCombo.setItems(FXCollections.observableArrayList(categories));
            categoryCombo.setConverter(new javafx.util.StringConverter<Category>() {
                @Override
                public String toString(Category category) {
                    return category != null ? category.getCategoryName() : "";
                }

                @Override
                public Category fromString(String string) {
                    return null;
                }
            });
        } catch (SQLException e) {
            logger.error("Error loading categories", e);
            showError("Failed to load categories: " + e.getMessage());
        }
    }

    private void loadSuppliers() {
        try {
            List<Supplier> suppliers = supplierDAO.getAll();
            supplierCombo.setItems(FXCollections.observableArrayList(suppliers));
            supplierCombo.setConverter(new javafx.util.StringConverter<Supplier>() {
                @Override
                public String toString(Supplier supplier) {
                    return supplier != null ? supplier.getSupplierName() : "";
                }

                @Override
                public Supplier fromString(String string) {
                    return null;
                }
            });
        } catch (SQLException e) {
            logger.error("Error loading suppliers", e);
            showError("Failed to load suppliers: " + e.getMessage());
        }
    }

    private void setupValidation() {
        // Numeric validation for quantity
        quantityField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                quantityField.setText(oldValue);
            }
        });

        // Numeric validation for price
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d{0,2}")) {
                priceField.setText(oldValue);
            }
        });

        // Numeric validation for min stock
        minStockField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                minStockField.setText(oldValue);
            }
        });
    }

    private boolean validateForm() {
        errorLabel.setText("");

        if (nameField.getText().trim().isEmpty()) {
            errorLabel.setText("Product name is required");
            nameField.requestFocus();
            return false;
        }

        if (skuField.getText().trim().isEmpty()) {
            errorLabel.setText("SKU is required");
            skuField.requestFocus();
            return false;
        }

        if (categoryCombo.getValue() == null) {
            errorLabel.setText("Please select a category");
            categoryCombo.requestFocus();
            return false;
        }

        if (supplierCombo.getValue() == null) {
            errorLabel.setText("Please select a supplier");
            supplierCombo.requestFocus();
            return false;
        }

        if (quantityField.getText().trim().isEmpty()) {
            errorLabel.setText("Quantity is required");
            quantityField.requestFocus();
            return false;
        }

        if (priceField.getText().trim().isEmpty()) {
            errorLabel.setText("Price is required");
            priceField.requestFocus();
            return false;
        }

        try {
            double price = Double.parseDouble(priceField.getText());
            if (price < 0) {
                errorLabel.setText("Price cannot be negative");
                priceField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Invalid price format");
            priceField.requestFocus();
            return false;
        }

        return true;
    }

    @FXML
    private void saveProduct() {
        if (!validateForm()) {
            return;
        }

        try {
            Product product = new Product();
            product.setProductCode(skuField.getText().trim());
            product.setProductName(nameField.getText().trim());
            product.setDescription(descriptionArea.getText().trim());
            product.setCategoryId(categoryCombo.getValue().getCategoryId());
            product.setSupplierId(supplierCombo.getValue().getSupplierId());
            product.setUnitPrice(new BigDecimal(priceField.getText().trim()));
            product.setQuantityInStock(Integer.parseInt(quantityField.getText().trim()));
            product.setReorderLevel(
                    minStockField.getText().isEmpty() ? 10 : Integer.parseInt(minStockField.getText().trim()));
            product.setUnitOfMeasure("pcs");
            product.setStatus("active");
            product.setCreatedAt(LocalDateTime.now());
            product.setUpdatedAt(LocalDateTime.now());

            boolean success = productService.addProduct(product);

            if (success) {
                saved = true;
                logger.info("Product added successfully: {}", product.getProductName());
                closeDialog();
            } else {
                errorLabel.setText("Failed to add product");
            }

        } catch (SQLException e) {
            logger.error("Error adding product", e);
            if (e.getMessage().contains("Duplicate entry")) {
                errorLabel.setText("SKU already exists");
            } else {
                errorLabel.setText("Database error: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            logger.error("Number format error", e);
            errorLabel.setText("Invalid number format");
        }
    }

    @FXML
    private void cancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    public boolean isSaved() {
        return saved;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}