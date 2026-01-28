package com.inventory.controller;

import com.inventory.dao.*;
import com.inventory.model.*;
import com.inventory.service.ProductService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * ProductController - Handles product management view with CRUD operations
 */
public class ProductController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<Category> categoryFilterCombo;
    @FXML
    private ComboBox<Supplier> supplierFilterCombo;
    @FXML
    private ComboBox<String> stockStatusCombo;

    @FXML
    private TableView<Product> productsTable;
    @FXML
    private TableColumn<Product, Integer> productIdCol;
    @FXML
    private TableColumn<Product, String> productNameCol;
    @FXML
    private TableColumn<Product, String> productSkuCol;
    @FXML
    private TableColumn<Product, String> productCategoryCol;
    @FXML
    private TableColumn<Product, String> productSupplierCol;
    @FXML
    private TableColumn<Product, Integer> productQuantityCol;
    @FXML
    private TableColumn<Product, Double> productPriceCol;
    @FXML
    private TableColumn<Product, Void> productActionsCol;

    @FXML
    private Label productCountLabel;
    @FXML
    private Label pageLabel;

    private final ProductService productService = new ProductService();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();

    private ObservableList<Product> allProducts = FXCollections.observableArrayList();
    private ObservableList<Product> filteredProducts = FXCollections.observableArrayList();

    private int currentPage = 1;
    private static final int ITEMS_PER_PAGE = 20;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initializing ProductController");

        setupTableColumns();
        // Ensure columns auto-size nicely like other controllers
        productsTable.setColumnResizePolicy(javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY);
        setupFilters();
        loadProducts();
        setupSearchListener();
    }

    /**
     * Setup table columns
     */
    private void setupTableColumns() {
        productIdCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        productNameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        productSkuCol.setCellValueFactory(new PropertyValueFactory<>("productCode"));

        productCategoryCol.setCellValueFactory(cellData -> {
            try {
                Category category = categoryDAO.getById(cellData.getValue().getCategoryId());
                return new javafx.beans.property.SimpleStringProperty(
                        category != null ? category.getName() : "N/A");
            } catch (SQLException e) {
                return new javafx.beans.property.SimpleStringProperty("Error");
            }
        });

        productSupplierCol.setCellValueFactory(cellData -> {
            try {
                Supplier supplier = supplierDAO.getById(cellData.getValue().getSupplierId());
                return new javafx.beans.property.SimpleStringProperty(
                        supplier != null ? supplier.getName() : "N/A");
            } catch (SQLException e) {
                return new javafx.beans.property.SimpleStringProperty("Error");
            }
        });

        productQuantityCol.setCellValueFactory(new PropertyValueFactory<>("quantityInStock"));

        productPriceCol.setCellValueFactory(cellData -> {
            double price = cellData.getValue().getUnitPrice() != null
                    ? cellData.getValue().getUnitPrice().doubleValue()
                    : 0.0;
            return new javafx.beans.property.SimpleDoubleProperty(price).asObject();
        });

        addActionButtons();
    }

    /**
     * Add action buttons to table
     */
    private void addActionButtons() {
        productActionsCol.setCellFactory(param -> new TableCell<Product, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");

            {
                editBtn.getStyleClass().add("secondary-button");
                deleteBtn.getStyleClass().add("danger-button");

                editBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5px 10px;");
                deleteBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5px 10px;");

                editBtn.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    editProduct(product);
                });

                deleteBtn.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    deleteProduct(product);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(5, editBtn, deleteBtn);
                    setGraphic(buttons);
                }
            }
        });
    }

    /**
     * Setup filter dropdowns
     */
    private void setupFilters() {
        try {
            // Load categories
            List<Category> categories = categoryDAO.getAll();
            categoryFilterCombo.setItems(FXCollections.observableArrayList(categories));
            categoryFilterCombo.setPromptText("All Categories");

            // Load suppliers
            List<Supplier> suppliers = supplierDAO.getAll();
            supplierFilterCombo.setItems(FXCollections.observableArrayList(suppliers));
            supplierFilterCombo.setPromptText("All Suppliers");

            // Stock status options
            stockStatusCombo.setItems(FXCollections.observableArrayList(
                    "All", "In Stock", "Low Stock", "Out of Stock"));
            stockStatusCombo.setValue("All");

            // Add listeners
            categoryFilterCombo.setOnAction(e -> applyFilters());
            supplierFilterCombo.setOnAction(e -> applyFilters());
            stockStatusCombo.setOnAction(e -> applyFilters());

        } catch (SQLException e) {
            logger.error("Error loading filters", e);
            showError("Failed to load filters: " + e.getMessage());
        }
    }

    /**
     * Setup search field listener
     */
    private void setupSearchListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });
    }

    /**
     * Load all products
     */
    private void loadProducts() {
        try {
            List<Product> products = productService.getAllProducts();
            allProducts.setAll(products);
            filteredProducts.setAll(products);
            logger.info("Loaded {} products into controller", products.size());
            if (!products.isEmpty()) {
                logger.info("First product sample: {}", products.get(0).getProductName());
            }
            updateTable();
            productsTable.refresh();
            updateProductCount();
        } catch (SQLException e) {
            logger.error("Error loading products", e);
            showError("Failed to load products: " + e.getMessage());
        }
    }

    /**
     * Apply filters
     */
    private void applyFilters() {
        List<Product> filtered = allProducts.stream()
                .filter(this::matchesSearchFilter)
                .filter(this::matchesCategoryFilter)
                .filter(this::matchesSupplierFilter)
                .filter(this::matchesStockStatusFilter)
                .collect(Collectors.toList());

        filteredProducts.setAll(filtered);
        currentPage = 1;
        updateTable();
        updateProductCount();
    }

    private boolean matchesSearchFilter(Product product) {
        String search = searchField.getText().toLowerCase();
        if (search.isEmpty())
            return true;
        return product.getProductName().toLowerCase().contains(search) ||
                product.getProductCode().toLowerCase().contains(search);
    }

    private boolean matchesCategoryFilter(Product product) {
        Category category = categoryFilterCombo.getValue();
        return category == null || product.getCategoryId() == category.getId();
    }

    private boolean matchesSupplierFilter(Product product) {
        Supplier supplier = supplierFilterCombo.getValue();
        return supplier == null || product.getSupplierId() == supplier.getId();
    }

    private boolean matchesStockStatusFilter(Product product) {
        String status = stockStatusCombo.getValue();
        if (status == null || status.equals("All"))
            return true;

        switch (status) {
            case "In Stock":
                return product.getQuantityInStock() > product.getReorderLevel();
            case "Low Stock":
                return product.getQuantityInStock() > 0 && product.getQuantityInStock() <= product.getReorderLevel();
            case "Out of Stock":
                return product.getQuantityInStock() == 0;
            default:
                return true;
        }
    }

    /**
     * Update table with current page
     */
    private void updateTable() {
        int fromIndex = (currentPage - 1) * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, filteredProducts.size());

        if (fromIndex < filteredProducts.size()) {
            List<Product> pageItems = filteredProducts.subList(fromIndex, toIndex);
            productsTable.setItems(FXCollections.observableArrayList(pageItems));
        } else {
            productsTable.setItems(FXCollections.observableArrayList());
        }

        updatePageLabel();
    }

    /**
     * Update product count label
     */
    private void updateProductCount() {
        productCountLabel.setText("Showing " + filteredProducts.size() + " products");
    }

    /**
     * Update page label
     */
    private void updatePageLabel() {
        int totalPages = (int) Math.ceil((double) filteredProducts.size() / ITEMS_PER_PAGE);
        pageLabel.setText("Page " + currentPage + " of " + Math.max(1, totalPages));
    }

    /**
     * Add new product
     */
    @FXML
    private void addProduct() {
        logger.info("Add product clicked");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddProductDialog.fxml"));
            Parent root = loader.load();

            AddProductController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Add New Product");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            stage.showAndWait();

            if (controller.isSaved()) {
                loadProducts();
                showInfo("Product added successfully!");
            }

        } catch (IOException e) {
            logger.error("Error opening add product dialog", e);
            showError("Failed to open dialog: " + e.getMessage());
        }
    }

    /**
     * Edit product
     */
    private void editProduct(Product product) {
        logger.info("Edit product: {}", product.getProductName());
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditProductDialog.fxml"));
            Parent root = loader.load();

            EditProductController controller = loader.getController();
            controller.setProduct(product);

            Stage stage = new Stage();
            stage.setTitle("Edit Product");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            stage.showAndWait();

            if (controller.isSaved()) {
                loadProducts();
                showInfo("Product updated successfully!");
            }

        } catch (IOException e) {
            logger.error("Error opening edit product dialog", e);
            showError("Failed to open dialog: " + e.getMessage());
        }
    }

    /**
     * Delete product
     */
    private void deleteProduct(Product product) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Product");
        confirm.setContentText("Are you sure you want to delete " + product.getProductName() + "?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    productService.deleteProduct(product.getProductId());
                    loadProducts();
                    showInfo("Product deleted successfully");
                } catch (SQLException e) {
                    logger.error("Error deleting product", e);
                    showError("Failed to delete product: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Clear filters
     */
    @FXML
    private void clearFilters() {
        searchField.clear();
        categoryFilterCombo.setValue(null);
        supplierFilterCombo.setValue(null);
        stockStatusCombo.setValue("All");
        applyFilters();
    }

    /**
     * Previous page
     */
    @FXML
    private void previousPage() {
        if (currentPage > 1) {
            currentPage--;
            updateTable();
        }
    }

    /**
     * Next page
     */
    @FXML
    private void nextPage() {
        int totalPages = (int) Math.ceil((double) filteredProducts.size() / ITEMS_PER_PAGE);
        if (currentPage < totalPages) {
            currentPage++;
            updateTable();
        }
    }

    /**
     * Export products
     */
    @FXML
    private void exportProducts() {
        logger.info("Export products clicked");
        showInfo("Export functionality not yet implemented");
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}