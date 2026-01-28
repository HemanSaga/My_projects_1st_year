package com.inventory.controller;

import com.inventory.dao.*;
import com.inventory.model.*;
import com.inventory.service.ReportService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.ResourceBundle;

/**
 * DashboardController - Handles dashboard view
 */
public class DashboardController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @FXML
    private Label totalProductsLabel;
    @FXML
    private Label lowStockLabel;
    @FXML
    private Label totalCategoriesLabel;
    @FXML
    private Label totalSuppliersLabel;

    @FXML
    private LineChart<String, Number> transactionChart;
    @FXML
    private PieChart categoryChart;

    @FXML
    private TableView<StockTransaction> recentTransactionsTable;
    @FXML
    private TableColumn<StockTransaction, String> transactionDateCol;
    @FXML
    private TableColumn<StockTransaction, String> transactionProductCol;
    @FXML
    private TableColumn<StockTransaction, String> transactionTypeCol;
    @FXML
    private TableColumn<StockTransaction, Integer> transactionQuantityCol;
    @FXML
    private TableColumn<StockTransaction, String> transactionUserCol;

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final StockTransactionDAO transactionDAO = new StockTransactionDAO();
    private final LowStockAlertDAO alertDAO = new LowStockAlertDAO();
    private final ReportService reportService = new ReportService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initializing DashboardController");

        setupTableColumns();
        loadDashboardData();
    }

    /**
     * Setup table columns
     */
    private void setupTableColumns() {
        transactionDateCol.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getTransactionDate();
            String formatted = date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
            return new javafx.beans.property.SimpleStringProperty(formatted);
        });

        transactionProductCol.setCellValueFactory(cellData -> {
            try {
                Product product = productDAO.getById(cellData.getValue().getProductId());
                return new javafx.beans.property.SimpleStringProperty(
                        product != null ? product.getName() : "Unknown");
            } catch (SQLException e) {
                return new javafx.beans.property.SimpleStringProperty("Error");
            }
        });

        transactionTypeCol.setCellValueFactory(new PropertyValueFactory<>("transactionType"));
        transactionQuantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        transactionUserCol.setCellValueFactory(new PropertyValueFactory<>("username"));
    }

    /**
     * Load all dashboard data
     */
    private void loadDashboardData() {
        try {
            loadStatistics();
            loadTransactionChart();
            loadCategoryChart();
            loadRecentTransactions();
        } catch (SQLException e) {
            logger.error("Error loading dashboard data", e);
            showError("Failed to load dashboard data: " + e.getMessage());
        }
    }

    /**
     * Load statistics cards
     */
    private void loadStatistics() throws SQLException {
        // Total products
        int totalProducts = productDAO.getCount();
        totalProductsLabel.setText(String.valueOf(totalProducts));

        // Low stock alerts
        int lowStockCount = alertDAO.getActiveAlerts().size();
        lowStockLabel.setText(String.valueOf(lowStockCount));

        // Total categories
        int totalCategories = categoryDAO.getAll().size();
        totalCategoriesLabel.setText(String.valueOf(totalCategories));

        // Total suppliers
        int totalSuppliers = supplierDAO.getAll().size();
        totalSuppliersLabel.setText(String.valueOf(totalSuppliers));
    }

    /**
     * Load transaction trend chart
     */
    private void loadTransactionChart() throws SQLException {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(7);

        Map<String, Map<String, Integer>> trends = reportService.getTransactionTrends(startDate, endDate);

        XYChart.Series<String, Number> inSeries = new XYChart.Series<>();
        inSeries.setName("Stock In");

        XYChart.Series<String, Number> outSeries = new XYChart.Series<>();
        outSeries.setName("Stock Out");

        for (Map.Entry<String, Map<String, Integer>> entry : trends.entrySet()) {
            String date = entry.getKey().substring(5); // Get MM-DD part
            Map<String, Integer> data = entry.getValue();

            inSeries.getData().add(new XYChart.Data<>(date, data.get("IN")));
            outSeries.getData().add(new XYChart.Data<>(date, data.get("OUT")));
        }

        transactionChart.getData().clear();
        transactionChart.getData().addAll(inSeries, outSeries);
    }

    /**
     * Load category distribution chart
     */
    private void loadCategoryChart() throws SQLException {
        Map<String, Integer> distribution = reportService.getProductsByCategoryDistribution();

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        for (Map.Entry<String, Integer> entry : distribution.entrySet()) {
            if (entry.getValue() > 0) {
                pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }
        }

        categoryChart.setData(pieChartData);
    }

    /**
     * Load recent transactions
     */
    private void loadRecentTransactions() throws SQLException {
        List<StockTransaction> recentTransactions = transactionDAO.getRecent(10);
        ObservableList<StockTransaction> data = FXCollections.observableArrayList(recentTransactions);
        recentTransactionsTable.setItems(data);
    }

    /**
     * Refresh dashboard
     */
    @FXML
    private void refreshDashboard() {
        logger.info("Refreshing dashboard");
        loadDashboardData();
    }

    /**
     * Show error alert
     */
    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
