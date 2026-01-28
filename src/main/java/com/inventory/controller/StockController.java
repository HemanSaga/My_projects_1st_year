package com.inventory.controller;

import com.inventory.dao.*;
import com.inventory.model.*;
import com.inventory.service.StockService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * StockController - FIXED VERSION
 * Properly displays stock transactions in table
 */
public class StockController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(StockController.class);

    @FXML
    private Label totalStockValueLabel;
    @FXML
    private Label todayTransactionsLabel;
    @FXML
    private Label lastUpdatedLabel;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private ComboBox<String> transactionTypeCombo;
    @FXML
    private TextField searchField;
    @FXML
    private TableView<StockTransaction> transactionsTable;
    @FXML
    private TableColumn<StockTransaction, Integer> transactionIdCol;
    @FXML
    private TableColumn<StockTransaction, String> transactionDateCol;
    @FXML
    private TableColumn<StockTransaction, String> transactionProductCol;
    @FXML
    private TableColumn<StockTransaction, String> transactionTypeCol;
    @FXML
    private TableColumn<StockTransaction, Integer> transactionQuantityCol;
    @FXML
    private TableColumn<StockTransaction, Double> transactionPriceCol;
    @FXML
    private TableColumn<StockTransaction, Double> transactionTotalCol;
    @FXML
    private TableColumn<StockTransaction, String> transactionUserCol;
    @FXML
    private TableColumn<StockTransaction, String> transactionNotesCol;
    @FXML
    private Label transactionCountLabel;
    @FXML
    private Label pageLabel;

    private final StockService stockService = new StockService();
    private final ProductDAO productDAO = new ProductDAO();

    private ObservableList<StockTransaction> allTransactions = FXCollections.observableArrayList();
    private ObservableList<StockTransaction> filteredTransactions = FXCollections.observableArrayList();

    private int currentPage = 1;
    private static final int ITEMS_PER_PAGE = 20;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initializing StockController");

        // CRITICAL: Setup BEFORE loading
        setupTableColumns();
        setupFilters();
        setupSearchListener();

        // NOW load data
        loadStatistics();
        loadTransactions();
    }

    /**
     * FIXED: Setup table columns with proper cell value factories
     */
    private void setupTableColumns() {
        logger.info("Setting up table columns...");

        // ID Column
        transactionIdCol.setCellValueFactory(cellData -> cellData.getValue().transactionIdProperty().asObject());

        // Date Column
        transactionDateCol.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getTransactionDate();
            String formatted = date != null ? date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) : "N/A";
            return new javafx.beans.property.SimpleStringProperty(formatted);
        });

        // Product Column
        transactionProductCol.setCellValueFactory(cellData -> {
            try {
                Product product = productDAO.getById(cellData.getValue().getProductId());
                return new javafx.beans.property.SimpleStringProperty(
                        product != null ? product.getProductName() : "Unknown");
            } catch (SQLException e) {
                logger.error("Error getting product name", e);
                return new javafx.beans.property.SimpleStringProperty("Error");
            }
        });

        // Type Column
        transactionTypeCol.setCellValueFactory(cellData -> cellData.getValue().transactionTypeProperty());

        // Quantity Column
        transactionQuantityCol.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());

        // Price Column
        transactionPriceCol.setCellValueFactory(cellData -> cellData.getValue().unitPriceProperty().asObject());

        // Total Column
        transactionTotalCol.setCellValueFactory(cellData -> cellData.getValue().totalPriceProperty().asObject());

        // User Column
        transactionUserCol.setCellValueFactory(cellData -> cellData.getValue().performedByProperty());

        // Notes Column
        transactionNotesCol.setCellValueFactory(cellData -> cellData.getValue().notesProperty());

        logger.info("✓ Table columns configured");
    }

    private void setupFilters() {
        transactionTypeCombo.setItems(
                FXCollections.observableArrayList("All", "stock_in", "stock_out"));
        transactionTypeCombo.setValue("All");
        transactionTypeCombo.setOnAction(e -> applyFilters());

        startDatePicker.setValue(LocalDate.now().minusDays(30));
        endDatePicker.setValue(LocalDate.now());
    }

    private void setupSearchListener() {
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                applyFilters();
            });
        }
    }

    private void loadStatistics() {
        try {
            double totalValue = stockService.calculateTotalStockValue();
            totalStockValueLabel.setText(String.format("₹%.2f", totalValue));

            int todayCount = stockService.getTodayTransactionCount();
            todayTransactionsLabel.setText(String.valueOf(todayCount));

            String lastUpdated = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("HH:mm"));
            lastUpdatedLabel.setText(lastUpdated);

        } catch (SQLException e) {
            logger.error("Error loading statistics", e);
            showError("Failed to load statistics: " + e.getMessage());
        }
    }

    /**
     * FIXED: Load transactions and populate table
     */
    private void loadTransactions() {
        try {
            logger.info("Loading transactions from database...");
            List<StockTransaction> transactions = stockService.getAllTransactions();
            logger.info("✓ Loaded {} transactions from database", transactions.size());

            // Clear and set all transactions
            allTransactions.clear();
            allTransactions.addAll(transactions);

            filteredTransactions.clear();
            filteredTransactions.addAll(transactions);

            // Update table
            updateTable();
            updateTransactionCount();

            logger.info("✓ Transactions set to table");

        } catch (SQLException e) {
            logger.error("Error loading transactions", e);
            showError("Failed to load transactions: " + e.getMessage());
        }
    }

    @FXML
    private void applyFilters() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        String typeFilter = transactionTypeCombo.getValue();
        String search = searchField != null ? searchField.getText().toLowerCase() : "";

        List<StockTransaction> filtered = allTransactions.stream()
                .filter(t -> matchesDateFilter(t, startDate, endDate))
                .filter(t -> matchesTypeFilter(t, typeFilter))
                .filter(t -> matchesSearchFilter(t, search))
                .collect(Collectors.toList());

        filteredTransactions.clear();
        filteredTransactions.addAll(filtered);
        currentPage = 1;
        updateTable();
        updateTransactionCount();
    }

    private boolean matchesDateFilter(StockTransaction transaction, LocalDate start, LocalDate end) {
        if (start == null || end == null)
            return true;
        LocalDate transactionDate = transaction.getTransactionDate().toLocalDate();
        return !transactionDate.isBefore(start) && !transactionDate.isAfter(end);
    }

    private boolean matchesTypeFilter(StockTransaction transaction, String type) {
        return type == null || type.equals("All") ||
                transaction.getTransactionType().equalsIgnoreCase(type);
    }

    private boolean matchesSearchFilter(StockTransaction transaction, String search) {
        if (search.isEmpty())
            return true;
        try {
            Product product = productDAO.getById(transaction.getProductId());
            return product != null &&
                    product.getProductName().toLowerCase().contains(search);
        } catch (SQLException e) {
            return false;
        }
    }

    private void updateTable() {
        int fromIndex = (currentPage - 1) * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, filteredTransactions.size());

        if (fromIndex < filteredTransactions.size()) {
            List<StockTransaction> pageItems = filteredTransactions.subList(fromIndex, toIndex);
            transactionsTable.setItems(FXCollections.observableArrayList(pageItems));
        } else {
            transactionsTable.setItems(FXCollections.observableArrayList());
        }

        updatePageLabel();
    }

    private void updateTransactionCount() {
        transactionCountLabel.setText("Showing " + filteredTransactions.size() + " transactions");
    }

    private void updatePageLabel() {
        int totalPages = (int) Math.ceil((double) filteredTransactions.size() / ITEMS_PER_PAGE);
        pageLabel.setText("Page " + currentPage + " of " + Math.max(1, totalPages));
    }

    @FXML
    private void stockIn() {
        logger.info("Stock in clicked");

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Stock In");
        dialog.setHeaderText("Add Stock");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Product> productCombo = new ComboBox<>();
        TextField quantityField = new TextField();
        TextField refField = new TextField();
        TextArea notesArea = new TextArea();
        notesArea.setPrefRowCount(3);

        try {
            List<Product> products = productDAO.getAll();
            productCombo.setItems(FXCollections.observableArrayList(products));
            productCombo.setConverter(new javafx.util.StringConverter<Product>() {
                @Override
                public String toString(Product p) {
                    return p != null ? p.getProductName() + " (" + p.getProductCode() + ")" : "";
                }

                @Override
                public Product fromString(String s) {
                    return null;
                }
            });
        } catch (SQLException e) {
            showError("Failed to load products");
            return;
        }

        grid.add(new Label("Product:"), 0, 0);
        grid.add(productCombo, 1, 0);
        grid.add(new Label("Quantity:"), 0, 1);
        grid.add(quantityField, 1, 1);
        grid.add(new Label("Reference:"), 0, 2);
        grid.add(refField, 1, 2);
        grid.add(new Label("Notes:"), 0, 3);
        grid.add(notesArea, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Product product = productCombo.getValue();
                if (product == null) {
                    showError("Please select a product");
                    return;
                }

                int quantity = Integer.parseInt(quantityField.getText());
                if (quantity <= 0) {
                    showError("Quantity must be positive");
                    return;
                }

                boolean success = stockService.stockIn(
                        product.getProductId(),
                        quantity,
                        product.getUnitPrice().doubleValue(),
                        notesArea.getText(),
                        "Admin");

                if (success) {
                    loadStatistics();
                    loadTransactions();
                    showInfo("Stock added successfully!");
                }
            } catch (NumberFormatException e) {
                showError("Invalid quantity. Please enter a number.");
            } catch (Exception e) {
                logger.error("Error in stock in", e);
                showError("Error: " + e.getMessage());
            }
        }
    }

    @FXML
    private void stockOut() {
        logger.info("Stock out clicked");

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Stock Out");
        dialog.setHeaderText("Remove Stock");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<Product> productCombo = new ComboBox<>();
        TextField quantityField = new TextField();
        TextField refField = new TextField();
        TextArea notesArea = new TextArea();
        notesArea.setPrefRowCount(3);

        try {
            List<Product> products = productDAO.getAll();
            productCombo.setItems(FXCollections.observableArrayList(products));
            productCombo.setConverter(new javafx.util.StringConverter<Product>() {
                @Override
                public String toString(Product p) {
                    return p != null ? p.getProductName() + " (Stock: " + p.getQuantityInStock() + ")" : "";
                }

                @Override
                public Product fromString(String s) {
                    return null;
                }
            });
        } catch (SQLException e) {
            showError("Failed to load products");
            return;
        }

        grid.add(new Label("Product:"), 0, 0);
        grid.add(productCombo, 1, 0);
        grid.add(new Label("Quantity:"), 0, 1);
        grid.add(quantityField, 1, 1);
        grid.add(new Label("Reference:"), 0, 2);
        grid.add(refField, 1, 2);
        grid.add(new Label("Notes:"), 0, 3);
        grid.add(notesArea, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Product product = productCombo.getValue();
                if (product == null) {
                    showError("Please select a product");
                    return;
                }

                int quantity = Integer.parseInt(quantityField.getText());
                if (quantity <= 0) {
                    showError("Quantity must be positive");
                    return;
                }

                boolean success = stockService.stockOut(
                        product.getProductId(),
                        quantity,
                        product.getUnitPrice().doubleValue(),
                        notesArea.getText(),
                        "Admin");

                if (success) {
                    loadStatistics();
                    loadTransactions();
                    showInfo("Stock removed successfully!");
                }
            } catch (NumberFormatException e) {
                showError("Invalid quantity. Please enter a number.");
            } catch (IllegalArgumentException e) {
                showError(e.getMessage());
            } catch (Exception e) {
                logger.error("Error in stock out", e);
                showError("Error: " + e.getMessage());
            }
        }
    }

    @FXML
    private void clearFilters() {
        startDatePicker.setValue(LocalDate.now().minusDays(30));
        endDatePicker.setValue(LocalDate.now());
        transactionTypeCombo.setValue("All");
        if (searchField != null)
            searchField.clear();
        applyFilters();
    }

    @FXML
    private void previousPage() {
        if (currentPage > 1) {
            currentPage--;
            updateTable();
        }
    }

    @FXML
    private void nextPage() {
        int totalPages = (int) Math.ceil((double) filteredTransactions.size() / ITEMS_PER_PAGE);
        if (currentPage < totalPages) {
            currentPage++;
            updateTable();
        }
    }

    @FXML
    private void exportReport() {
        try {
            File file = new File("stock_transactions_export.csv");
            FileWriter writer = new FileWriter(file);

            writer.write("ID,Date,Product,Type,Quantity,User,Notes\n");

            for (StockTransaction t : filteredTransactions) {
                Product p = productDAO.getById(t.getProductId());
                String date = t.getTransactionDate().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                writer.write(String.format("%d,\"%s\",\"%s\",\"%s\",%d,\"%s\",\"%s\"\n",
                        t.getTransactionId(),
                        date,
                        p != null ? p.getProductName() : "Unknown",
                        t.getTransactionType(),
                        t.getQuantity(),
                        t.getPerformedBy(),
                        t.getNotes()));
            }

            writer.close();
            showInfo("Transactions exported to: " + file.getAbsolutePath());

        } catch (Exception e) {
            logger.error("Error exporting", e);
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