package com.inventory.controller;

import com.inventory.dao.SupplierDAO;
import com.inventory.dao.ProductDAO;
import com.inventory.model.Supplier;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * SupplierController - MODULE-SAFE VERSION
 * Uses Callback interface to bypass JavaFX module access issues
 */
public class SupplierController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(SupplierController.class);

    @FXML
    private Label totalSuppliersLabel;
    @FXML
    private Label activeSuppliersLabel;
    @FXML
    private Label totalProductsLabel;
    @FXML
    private Label pendingOrdersLabel;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> statusFilterCombo;
    @FXML
    private TableView<Supplier> suppliersTable;
    @FXML
    private TableColumn<Supplier, Number> supplierIdCol;
    @FXML
    private TableColumn<Supplier, String> supplierNameCol;
    @FXML
    private TableColumn<Supplier, String> supplierContactCol;
    @FXML
    private TableColumn<Supplier, String> supplierEmailCol;
    @FXML
    private TableColumn<Supplier, String> supplierPhoneCol;
    @FXML
    private TableColumn<Supplier, String> supplierAddressCol;
    @FXML
    private TableColumn<Supplier, Number> supplierProductsCol;
    @FXML
    private TableColumn<Supplier, String> supplierStatusCol;
    @FXML
    private TableColumn<Supplier, Void> supplierActionsCol;
    @FXML
    private Label supplierCountLabel;
    @FXML
    private Label selectedSupplierLabel;
    @FXML
    private Label selectedContactLabel;
    @FXML
    private Label selectedEmailLabel;
    @FXML
    private Label selectedPhoneLabel;
    @FXML
    private Label selectedProductCountLabel;
    @FXML
    private Label selectedStatusLabel;

    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private ObservableList<Supplier> allSuppliers = FXCollections.observableArrayList();
    private Supplier selectedSupplier = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("==========================================");
        logger.info("Initializing SupplierController - MODULE-SAFE VERSION");
        logger.info("==========================================");

        setupTableColumns();
        // Ensure columns auto-size nicely
        suppliersTable.setColumnResizePolicy(javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY);
        setupFilters();
        setupSearchListener();
        setupTableSelectionListener();
        loadSuppliers();
    }

    /**
     * MODULE-SAFE: Uses Callback interface instead of direct property access
     * This bypasses JavaFX module system restrictions
     */
    private void setupTableColumns() {
        logger.info("Setting up table columns (module-safe)...");

        // ID Column - Using Callback with SimpleIntegerProperty
        supplierIdCol.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Supplier, Number>, ObservableValue<Number>>() {
                    @Override
                    public ObservableValue<Number> call(TableColumn.CellDataFeatures<Supplier, Number> param) {
                        Supplier supplier = param.getValue();
                        return new SimpleIntegerProperty(supplier.getSupplierId());
                    }
                });

        // Name Column - Using Callback with SimpleStringProperty
        supplierNameCol.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Supplier, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<Supplier, String> param) {
                        Supplier supplier = param.getValue();
                        String name = supplier.getSupplierName();
                        return new SimpleStringProperty(name != null ? name : "");
                    }
                });

        // Contact Column
        supplierContactCol.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Supplier, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<Supplier, String> param) {
                        Supplier supplier = param.getValue();
                        String contact = supplier.getContactPerson();
                        return new SimpleStringProperty(contact != null ? contact : "");
                    }
                });

        // Email Column
        supplierEmailCol.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Supplier, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<Supplier, String> param) {
                        Supplier supplier = param.getValue();
                        String email = supplier.getEmail();
                        return new SimpleStringProperty(email != null ? email : "");
                    }
                });

        // Phone Column
        supplierPhoneCol.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Supplier, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<Supplier, String> param) {
                        Supplier supplier = param.getValue();
                        String phone = supplier.getPhone();
                        return new SimpleStringProperty(phone != null ? phone : "");
                    }
                });

        // Address Column
        supplierAddressCol.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Supplier, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<Supplier, String> param) {
                        Supplier supplier = param.getValue();
                        String address = supplier.getAddress();
                        return new SimpleStringProperty(address != null ? address : "");
                    }
                });

        // Products Count Column
        supplierProductsCol.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Supplier, Number>, ObservableValue<Number>>() {
                    @Override
                    public ObservableValue<Number> call(TableColumn.CellDataFeatures<Supplier, Number> param) {
                        try {
                            int count = productDAO.getBySupplier(param.getValue().getSupplierId()).size();
                            return new SimpleIntegerProperty(count);
                        } catch (SQLException e) {
                            logger.error("Error getting product count", e);
                            return new SimpleIntegerProperty(0);
                        }
                    }
                });

        // Status Column
        supplierStatusCol.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Supplier, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<Supplier, String> param) {
                        return new SimpleStringProperty("Active");
                    }
                });

        // Action buttons
        addActionButtons();

        logger.info("✓ Table columns configured (module-safe)");
    }

    private void addActionButtons() {
        if (supplierActionsCol != null) {
            supplierActionsCol.setCellFactory(param -> new TableCell<Supplier, Void>() {
                private final Button editBtn = new Button("✏️");
                private final Button deleteBtn = new Button("🗑️");

                {
                    editBtn.getStyleClass().add("secondary-button");
                    deleteBtn.getStyleClass().add("danger-button");
                    editBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5px 10px;");
                    deleteBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5px 10px;");

                    editBtn.setOnAction(event -> {
                        Supplier supplier = getTableView().getItems().get(getIndex());
                        editSupplier(supplier);
                    });

                    deleteBtn.setOnAction(event -> {
                        Supplier supplier = getTableView().getItems().get(getIndex());
                        deleteSupplier(supplier);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        HBox buttons = new HBox(5, editBtn, deleteBtn);
                        setGraphic(buttons);
                    }
                }
            });
        }
    }

    private void setupFilters() {
        if (statusFilterCombo != null) {
            statusFilterCombo.setItems(FXCollections.observableArrayList("All", "Active"));
            statusFilterCombo.setValue("All");
            statusFilterCombo.setOnAction(e -> applyFilters());
        }
    }

    private void setupSearchListener() {
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                applyFilters();
            });
        }
    }

    private void setupTableSelectionListener() {
        suppliersTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        selectedSupplier = newValue;
                        updateDetailsPanel();
                    }
                });
    }

    private void loadSuppliers() {
        try {
            logger.info("==========================================");
            logger.info("Loading suppliers from database...");

            List<Supplier> suppliers = supplierDAO.getAll();
            logger.info("✓ Retrieved {} suppliers from DAO", suppliers.size());

            // Log each supplier
            for (int i = 0; i < suppliers.size(); i++) {
                Supplier s = suppliers.get(i);
                logger.info("  Supplier {}: ID={}, Name='{}', Email='{}'",
                        i + 1, s.getSupplierId(), s.getSupplierName(), s.getEmail());
            }

            allSuppliers.clear();
            allSuppliers.addAll(suppliers);
            logger.info("✓ Added {} suppliers to observable list", allSuppliers.size());

            suppliersTable.setItems(allSuppliers);
            logger.info("✓ Set items to table");

            // Force refresh
            suppliersTable.refresh();
            logger.info("✓ Table refreshed");

            int tableCount = suppliersTable.getItems().size();
            logger.info("✓ Table now contains {} items", tableCount);

            updateStatistics();
            updateSupplierCount();

            logger.info("==========================================");
            logger.info("✓ SUPPLIERS LOADED SUCCESSFULLY");
            logger.info("==========================================");

        } catch (SQLException e) {
            logger.error("==========================================");
            logger.error("❌ ERROR LOADING SUPPLIERS", e);
            logger.error("==========================================");
            showError("Failed to load suppliers: " + e.getMessage());
        }
    }

    private void updateStatistics() {
        try {
            totalSuppliersLabel.setText(String.valueOf(allSuppliers.size()));
            activeSuppliersLabel.setText(String.valueOf(allSuppliers.size()));
            int totalProducts = productDAO.getCount();
            totalProductsLabel.setText(String.valueOf(totalProducts));
            pendingOrdersLabel.setText("0");
        } catch (SQLException e) {
            logger.error("Error updating statistics", e);
        }
    }

    private void updateSupplierCount() {
        int displayCount = suppliersTable.getItems().size();
        supplierCountLabel.setText("Showing " + displayCount + " suppliers");
        logger.info("✓ Supplier count updated: {}", displayCount);
    }

    private void updateDetailsPanel() {
        if (selectedSupplier != null && selectedSupplierLabel != null) {
            selectedSupplierLabel.setText(selectedSupplier.getSupplierName());
            selectedContactLabel.setText(selectedSupplier.getContactPerson());
            selectedEmailLabel.setText(selectedSupplier.getEmail());
            selectedPhoneLabel.setText(selectedSupplier.getPhone());
            selectedStatusLabel.setText("Active");

            try {
                int productCount = productDAO.getBySupplier(selectedSupplier.getSupplierId()).size();
                selectedProductCountLabel.setText(String.valueOf(productCount));
            } catch (SQLException e) {
                selectedProductCountLabel.setText("Error");
            }
        }
    }

    private void applyFilters() {
        String search = searchField != null ? searchField.getText().toLowerCase() : "";

        ObservableList<Supplier> filtered = allSuppliers.filtered(supplier -> {
            boolean matchesSearch = search.isEmpty() ||
                    supplier.getSupplierName().toLowerCase().contains(search) ||
                    supplier.getEmail().toLowerCase().contains(search);
            return matchesSearch;
        });

        suppliersTable.setItems(filtered);
        updateSupplierCount();
    }

    @FXML
    private void addSupplier() {
        logger.info("Add supplier clicked");

        Dialog<Supplier> dialog = new Dialog<>();
        dialog.setTitle("Add Supplier");
        dialog.setHeaderText("Create New Supplier");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Supplier name");
        TextField contactField = new TextField();
        contactField.setPromptText("Contact person");
        TextField emailField = new TextField();
        emailField.setPromptText("email@example.com");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone number");
        TextArea addressArea = new TextArea();
        addressArea.setPrefRowCount(3);
        addressArea.setPromptText("Address");

        grid.add(new Label("Supplier Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Contact Person:"), 0, 1);
        grid.add(contactField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Phone:"), 0, 3);
        grid.add(phoneField, 1, 3);
        grid.add(new Label("Address:"), 0, 4);
        grid.add(addressArea, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Supplier supplier = new Supplier();
                supplier.setSupplierName(nameField.getText());
                supplier.setContactPerson(contactField.getText());
                supplier.setEmail(emailField.getText());
                supplier.setPhone(phoneField.getText());
                supplier.setAddress(addressArea.getText());
                return supplier;
            }
            return null;
        });

        Optional<Supplier> result = dialog.showAndWait();
        result.ifPresent(supplier -> {
            try {
                boolean success = supplierDAO.add(supplier);
                if (success) {
                    loadSuppliers();
                    showInfo("Supplier '" + supplier.getSupplierName() + "' added successfully!");
                } else {
                    showError("Failed to add supplier");
                }
            } catch (SQLException e) {
                logger.error("Error adding supplier", e);
                showError("Database error: " + e.getMessage());
            }
        });
    }

    private void editSupplier(Supplier supplier) {
        logger.info("Edit supplier: {}", supplier.getSupplierName());

        Dialog<Supplier> dialog = new Dialog<>();
        dialog.setTitle("Edit Supplier");
        dialog.setHeaderText("Edit Supplier Details");

        ButtonType saveButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(supplier.getSupplierName());
        TextField contactField = new TextField(supplier.getContactPerson());
        TextField emailField = new TextField(supplier.getEmail());
        TextField phoneField = new TextField(supplier.getPhone());
        TextArea addressArea = new TextArea(supplier.getAddress());
        addressArea.setPrefRowCount(3);

        grid.add(new Label("Supplier Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Contact Person:"), 0, 1);
        grid.add(contactField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Phone:"), 0, 3);
        grid.add(phoneField, 1, 3);
        grid.add(new Label("Address:"), 0, 4);
        grid.add(addressArea, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                supplier.setSupplierName(nameField.getText());
                supplier.setContactPerson(contactField.getText());
                supplier.setEmail(emailField.getText());
                supplier.setPhone(phoneField.getText());
                supplier.setAddress(addressArea.getText());
                return supplier;
            }
            return null;
        });

        Optional<Supplier> result = dialog.showAndWait();
        result.ifPresent(updatedSupplier -> {
            try {
                boolean success = supplierDAO.update(updatedSupplier);
                if (success) {
                    loadSuppliers();
                    showInfo("Supplier updated successfully!");
                } else {
                    showError("Failed to update supplier");
                }
            } catch (SQLException e) {
                logger.error("Error updating supplier", e);
                showError("Database error: " + e.getMessage());
            }
        });
    }

    private void deleteSupplier(Supplier supplier) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Supplier");
        confirm.setContentText("Are you sure you want to delete '" + supplier.getSupplierName() + "'?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    supplierDAO.delete(supplier.getSupplierId());
                    loadSuppliers();
                    showInfo("Supplier deleted successfully");
                } catch (SQLException e) {
                    logger.error("Error deleting supplier", e);
                    showError("Failed to delete supplier: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void clearFilters() {
        if (searchField != null)
            searchField.clear();
        if (statusFilterCombo != null)
            statusFilterCombo.setValue("All");
        applyFilters();
    }

    @FXML
    private void editSelectedSupplier() {
        if (selectedSupplier != null) {
            editSupplier(selectedSupplier);
        } else {
            showInfo("Please select a supplier first");
        }
    }

    @FXML
    private void deleteSelectedSupplier() {
        if (selectedSupplier != null) {
            deleteSupplier(selectedSupplier);
        } else {
            showInfo("Please select a supplier first");
        }
    }

    @FXML
    private void viewSupplierProducts() {
        if (selectedSupplier != null) {
            try {
                List<com.inventory.model.Product> products = productDAO.getBySupplier(selectedSupplier.getSupplierId());

                StringBuilder message = new StringBuilder();
                message.append("Total products: ").append(products.size()).append("\n\n");

                if (!products.isEmpty()) {
                    message.append("Products:\n");
                    for (com.inventory.model.Product p : products) {
                        message.append("- ").append(p.getProductName()).append("\n");
                    }
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Supplier Products");
                alert.setHeaderText(selectedSupplier.getSupplierName());
                alert.setContentText(message.toString());
                alert.showAndWait();

            } catch (SQLException e) {
                showError("Failed to load products: " + e.getMessage());
            }
        } else {
            showInfo("Please select a supplier first");
        }
    }

    @FXML
    private void emailSupplier() {
        if (selectedSupplier != null) {
            showInfo("Email: " + selectedSupplier.getEmail());
        } else {
            showInfo("Please select a supplier first");
        }
    }

    @FXML
    private void exportSuppliers() {
        try {
            File file = new File("suppliers_export.csv");
            FileWriter writer = new FileWriter(file);

            writer.write("ID,Supplier Name,Contact Person,Email,Phone,Address,Products\n");

            for (Supplier supplier : allSuppliers) {
                int productCount = productDAO.getBySupplier(supplier.getSupplierId()).size();

                writer.write(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d\n",
                        supplier.getSupplierId(),
                        supplier.getSupplierName(),
                        supplier.getContactPerson(),
                        supplier.getEmail(),
                        supplier.getPhone(),
                        supplier.getAddress(),
                        productCount));
            }

            writer.close();
            showInfo("Suppliers exported successfully!\n\nFile: " + file.getAbsolutePath());

        } catch (Exception e) {
            logger.error("Error exporting suppliers", e);
            showError("Failed to export: " + e.getMessage());
        }
    }

    @FXML
    private void emailAllSuppliers() {
        showInfo("Email All functionality not yet implemented");
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
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}