package com.inventory.controller;

import com.inventory.dao.*;
import com.inventory.model.*;
import com.inventory.service.StockService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AlertController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(AlertController.class);

    @FXML
    private Label criticalAlertsLabel;
    @FXML
    private Label warningAlertsLabel;
    @FXML
    private Label totalAlertsLabel;

    @FXML
    private ComboBox<String> severityFilterCombo;
    @FXML
    private ComboBox<Category> categoryFilterCombo;
    @FXML
    private TextField searchField;

    @FXML
    private TableView<LowStockAlert> alertsTable;
    @FXML
    private TableColumn<LowStockAlert, String> alertSeverityCol;
    @FXML
    private TableColumn<LowStockAlert, Integer> alertProductIdCol;
    @FXML
    private TableColumn<LowStockAlert, String> alertProductNameCol;
    @FXML
    private TableColumn<LowStockAlert, String> alertSkuCol;
    @FXML
    private TableColumn<LowStockAlert, String> alertCategoryCol;
    @FXML
    private TableColumn<LowStockAlert, Integer> alertCurrentStockCol;
    @FXML
    private TableColumn<LowStockAlert, Integer> alertThresholdCol;
    @FXML
    private TableColumn<LowStockAlert, String> alertSupplierCol;
    @FXML
    private TableColumn<LowStockAlert, Void> alertActionsCol;

    @FXML
    private Label alertCountLabel;

    private final StockService stockService = new StockService();
    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();

    private ObservableList<LowStockAlert> allAlerts = FXCollections.observableArrayList();
    private ObservableList<LowStockAlert> filteredAlerts = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initializing AlertController");

        setupTableColumns();
        setupFilters();
        loadAlerts();
        setupSearchListener();
    }

    private void setupTableColumns() {
        alertSeverityCol.setCellValueFactory(cellData -> {
            int stock = cellData.getValue().getCurrentStock();
            String severity = stock <= 5 ? "CRITICAL" : "WARNING";
            return new javafx.beans.property.SimpleStringProperty(severity);
        });

        alertSeverityCol.setCellFactory(column -> new TableCell<LowStockAlert, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("CRITICAL")) {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                    }
                }
            }
        });

        alertProductIdCol.setCellValueFactory(new PropertyValueFactory<>("productId"));

        alertProductNameCol.setCellValueFactory(cellData -> {
            try {
                Product product = productDAO.getById(cellData.getValue().getProductId());
                return new javafx.beans.property.SimpleStringProperty(
                        product != null ? product.getProductName() : "Unknown");
            } catch (SQLException e) {
                return new javafx.beans.property.SimpleStringProperty("Error");
            }
        });

        alertSkuCol.setCellValueFactory(cellData -> {
            try {
                Product product = productDAO.getById(cellData.getValue().getProductId());
                return new javafx.beans.property.SimpleStringProperty(
                        product != null ? product.getProductCode() : "N/A");
            } catch (SQLException e) {
                return new javafx.beans.property.SimpleStringProperty("Error");
            }
        });

        alertCategoryCol.setCellValueFactory(cellData -> {
            try {
                Product product = productDAO.getById(cellData.getValue().getProductId());
                if (product != null) {
                    Category category = categoryDAO.getById(product.getCategoryId());
                    return new javafx.beans.property.SimpleStringProperty(
                            category != null ? category.getCategoryName() : "N/A");
                }
                return new javafx.beans.property.SimpleStringProperty("N/A");
            } catch (SQLException e) {
                return new javafx.beans.property.SimpleStringProperty("Error");
            }
        });

        alertCurrentStockCol.setCellValueFactory(new PropertyValueFactory<>("currentStock"));
        alertThresholdCol.setCellValueFactory(new PropertyValueFactory<>("minStockLevel"));

        alertSupplierCol.setCellValueFactory(cellData -> {
            try {
                Product product = productDAO.getById(cellData.getValue().getProductId());
                if (product != null) {
                    Supplier supplier = supplierDAO.getById(product.getSupplierId());
                    return new javafx.beans.property.SimpleStringProperty(
                            supplier != null ? supplier.getSupplierName() : "N/A");
                }
                return new javafx.beans.property.SimpleStringProperty("N/A");
            } catch (SQLException e) {
                return new javafx.beans.property.SimpleStringProperty("Error");
            }
        });

        addActionButtons();
    }

    private void addActionButtons() {
        alertActionsCol.setCellFactory(param -> new TableCell<LowStockAlert, Void>() {
            private final Button resolveBtn = new Button("Resolve");

            {
                resolveBtn.getStyleClass().add("secondary-button");
                resolveBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5px 10px;");

                resolveBtn.setOnAction(event -> {
                    LowStockAlert alert = getTableView().getItems().get(getIndex());
                    resolveAlert(alert);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(resolveBtn);
                }
            }
        });
    }

    private void setupFilters() {
        try {
            severityFilterCombo.setItems(FXCollections.observableArrayList("All", "CRITICAL", "WARNING"));
            severityFilterCombo.setValue("All");
            severityFilterCombo.setOnAction(e -> applyFilters());

            List<Category> categories = categoryDAO.getAll();
            categoryFilterCombo.setItems(FXCollections.observableArrayList(categories));
            categoryFilterCombo.setPromptText("All Categories");
            categoryFilterCombo.setOnAction(e -> applyFilters());

        } catch (SQLException e) {
            logger.error("Error loading filters", e);
            showError("Failed to load filters: " + e.getMessage());
        }
    }

    private void setupSearchListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });
    }

    private void loadAlerts() {
        try {
            List<LowStockAlert> alerts = stockService.getActiveLowStockAlerts();
            allAlerts.setAll(alerts);
            filteredAlerts.setAll(alerts);
            updateTable();
            updateStatistics();
        } catch (SQLException e) {
            logger.error("Error loading alerts", e);
            showError("Failed to load alerts: " + e.getMessage());
        }
    }

    private void updateStatistics() {
        int critical = (int) allAlerts.stream()
                .filter(a -> a.getCurrentStock() <= 5)
                .count();

        int warning = (int) allAlerts.stream()
                .filter(a -> a.getCurrentStock() > 5 && a.getCurrentStock() <= 10)
                .count();

        criticalAlertsLabel.setText(String.valueOf(critical));
        warningAlertsLabel.setText(String.valueOf(warning));
        totalAlertsLabel.setText(String.valueOf(allAlerts.size()));
    }

    private void applyFilters() {
        String severity = severityFilterCombo.getValue();
        Category category = categoryFilterCombo.getValue();
        String search = searchField.getText().toLowerCase();

        List<LowStockAlert> filtered = allAlerts.stream()
                .filter(a -> matchesSeverityFilter(a, severity))
                .filter(a -> matchesCategoryFilter(a, category))
                .filter(a -> matchesSearchFilter(a, search))
                .collect(Collectors.toList());

        filteredAlerts.setAll(filtered);
        updateTable();
    }

    private boolean matchesSeverityFilter(LowStockAlert alert, String severity) {
        if (severity == null || severity.equals("All"))
            return true;
        int stock = alert.getCurrentStock();
        if (severity.equals("CRITICAL")) {
            return stock <= 5;
        } else {
            return stock > 5 && stock <= 10;
        }
    }

    private boolean matchesCategoryFilter(LowStockAlert alert, Category category) {
        if (category == null)
            return true;
        try {
            Product product = productDAO.getById(alert.getProductId());
            return product != null && product.getCategoryId() == category.getCategoryId();
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean matchesSearchFilter(LowStockAlert alert, String search) {
        if (search.isEmpty())
            return true;
        try {
            Product product = productDAO.getById(alert.getProductId());
            return product != null && product.getProductName().toLowerCase().contains(search);
        } catch (SQLException e) {
            return false;
        }
    }

    private void updateTable() {
        alertsTable.setItems(filteredAlerts);
        alertCountLabel.setText("Showing " + filteredAlerts.size() + " alerts");
    }

    private void resolveAlert(LowStockAlert alert) {
        try {
            stockService.resolveLowStockAlert(alert.getId());
            loadAlerts();
            showInfo("Alert resolved successfully");
        } catch (SQLException e) {
            logger.error("Error resolving alert", e);
            showError("Failed to resolve alert: " + e.getMessage());
        }
    }

    @FXML
    private void clearFilters() {
        severityFilterCombo.setValue("All");
        categoryFilterCombo.setValue(null);
        searchField.clear();
        applyFilters();
    }

    @FXML
    private void refreshAlerts() {
        loadAlerts();
    }

    @FXML
    private void exportAlerts() {
        try {
            File file = new File("low_stock_alerts_export.csv");
            FileWriter writer = new FileWriter(file);

            writer.write("Severity,Product ID,Product Name,SKU,Category,Current Stock,Threshold,Supplier\n");

            for (LowStockAlert alert : filteredAlerts) {
                Product p = productDAO.getById(alert.getProductId());
                Category c = p != null ? categoryDAO.getById(p.getCategoryId()) : null;
                Supplier s = p != null ? supplierDAO.getById(p.getSupplierId()) : null;

                String severity = alert.getCurrentStock() <= 5 ? "CRITICAL" : "WARNING";

                writer.write(String.format("\"%s\",%d,\"%s\",\"%s\",\"%s\",%d,%d,\"%s\"\n",
                        severity,
                        alert.getProductId(),
                        p != null ? p.getProductName() : "Unknown",
                        p != null ? p.getProductCode() : "N/A",
                        c != null ? c.getCategoryName() : "N/A",
                        alert.getCurrentStock(),
                        alert.getMinStockLevel(),
                        s != null ? s.getSupplierName() : "N/A"));
            }

            writer.close();
            showInfo("Alerts exported to: " + file.getAbsolutePath());

        } catch (Exception e) {
            showError("Failed to export: " + e.getMessage());
        }
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