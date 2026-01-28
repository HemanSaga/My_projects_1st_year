package com.inventory.controller;

import com.inventory.service.ReportService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

/**
 * ReportController - Handles reports and analytics view
 */
public class ReportController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    @FXML
    private Label totalRevenueLabel;
    @FXML
    private Label stockValueLabel;
    @FXML
    private Label totalTransactionsLabel;
    @FXML
    private Label avgTransactionLabel;
    @FXML
    private Label revenueChangeLabel;
    @FXML
    private Label stockChangeLabel;
    @FXML
    private Label transactionChangeLabel;
    @FXML
    private Label avgChangeLabel;

    @FXML
    private ComboBox<String> reportTypeCombo;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;

    @FXML
    private LineChart<String, Number> salesTrendChart;
    @FXML
    private PieChart stockByCategoryChart;
    @FXML
    private BarChart<String, Number> stockMovementChart;
    @FXML
    private TableView<?> topProductsTable;
    @FXML
    private TableView<?> supplierPerformanceTable;

    @FXML
    private ComboBox<String> rankingCriteriaCombo;
    @FXML
    private ComboBox<Integer> topCountCombo;

    @FXML
    private Label lastGeneratedLabel;

    private final ReportService reportService = new ReportService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initializing ReportController");

        setupFilters();
        loadDefaultReport();
    }

    /**
     * Setup filters
     */
    private void setupFilters() {
        // Report types
        reportTypeCombo.getItems().addAll(
                "Sales Report",
                "Stock Report",
                "Supplier Performance",
                "Top Products",
                "Inventory Turnover");
        reportTypeCombo.setValue("Sales Report");

        // Date range
        startDatePicker.setValue(LocalDate.now().minusDays(30));
        endDatePicker.setValue(LocalDate.now());

        // Ranking criteria
        if (rankingCriteriaCombo != null) {
            rankingCriteriaCombo.getItems().addAll(
                    "Quantity Sold",
                    "Revenue",
                    "Profit Margin");
            rankingCriteriaCombo.setValue("Quantity Sold");
        }

        // Top count
        if (topCountCombo != null) {
            topCountCombo.getItems().addAll(5, 10, 20, 50);
            topCountCombo.setValue(10);
        }
    }

    /**
     * Load default report
     */
    private void loadDefaultReport() {
        try {
            // Load default statistics
            totalRevenueLabel.setText("₹0.00");
            stockValueLabel.setText("₹0.00");
            totalTransactionsLabel.setText("0");
            avgTransactionLabel.setText("₹0.00");

            revenueChangeLabel.setText("--");
            stockChangeLabel.setText("--");
            transactionChangeLabel.setText("--");
            avgChangeLabel.setText("--");

            lastGeneratedLabel.setText("Last generated: Never");

        } catch (Exception e) {
            logger.error("Error loading default report", e);
            showError("Failed to load report: " + e.getMessage());
        }
    }

    /**
     * Generate report
     */
    @FXML
    private void generateReport() {
        logger.info("Generate report clicked");

        String reportType = reportTypeCombo.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null) {
            showError("Please select both start and end dates");
            return;
        }

        if (startDate.isAfter(endDate)) {
            showError("Start date must be before end date");
            return;
        }

        try {
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.atTime(23, 59, 59);

            switch (reportType) {
                case "Sales Report":
                    generateSalesReport(start, end);
                    break;
                case "Stock Report":
                    generateStockReport();
                    break;
                case "Supplier Performance":
                    generateSupplierReport();
                    break;
                case "Top Products":
                    generateTopProductsReport(start, end);
                    break;
                case "Inventory Turnover":
                    generateInventoryTurnoverReport(start, end);
                    break;
            }

            lastGeneratedLabel.setText("Last generated: " +
                    LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));

        } catch (Exception e) {
            logger.error("Error generating report", e);
            showError("Failed to generate report: " + e.getMessage());
        }
    }

    /**
     * Generate sales report
     */
    private void generateSalesReport(LocalDateTime start, LocalDateTime end) throws Exception {
        java.util.Map<String, Object> report = reportService.generateSalesReport(start, end);

        totalRevenueLabel.setText(String.format("₹%.2f", report.get("totalRevenue")));
        totalTransactionsLabel.setText(String.valueOf(report.get("totalTransactions")));
        avgTransactionLabel.setText(String.format("₹%.2f", report.get("avgTransaction")));

        showInfo("Sales report generated successfully");
    }

    /**
     * Generate stock report
     */
    private void generateStockReport() throws Exception {
        java.util.Map<String, Object> report = reportService.generateStockReport();

        stockValueLabel.setText(String.format("₹%.2f", report.get("totalStockValue")));
        totalTransactionsLabel.setText(String.valueOf(report.get("totalProducts")));

        showInfo("Stock report generated successfully");
    }

    /**
     * Generate supplier report
     */
    private void generateSupplierReport() throws Exception {
        java.util.List<java.util.Map<String, Object>> report = reportService.generateSupplierPerformanceReport();
        showInfo("Supplier performance report generated successfully");
    }

    /**
     * Generate top products report
     */
    private void generateTopProductsReport(LocalDateTime start, LocalDateTime end) throws Exception {
        int limit = topCountCombo != null ? topCountCombo.getValue() : 10;
        java.util.List<?> report = reportService.getTopSellingProducts(limit, start, end);
        showInfo("Top products report generated successfully");
    }

    /**
     * Generate inventory turnover report
     */
    private void generateInventoryTurnoverReport(LocalDateTime start, LocalDateTime end) throws Exception {
        double turnover = reportService.calculateInventoryTurnover(start, end);
        showInfo("Inventory turnover: " + String.format("%.2f", turnover));
    }

    /**
     * Export PDF
     */
    @FXML
    private void exportPDF() {
        logger.info("Export PDF clicked");
        showInfo("PDF export functionality not yet implemented");
    }

    /**
     * Export Excel
     */
    @FXML
    private void exportExcel() {
        logger.info("Export Excel clicked");
        showInfo("Excel export functionality not yet implemented");
    }

    /**
     * Email report
     */
    @FXML
    private void emailReport() {
        logger.info("Email report clicked");
        showInfo("Email functionality not yet implemented");
    }

    /**
     * Print report
     */
    @FXML
    private void printReport() {
        logger.info("Print report clicked");
        showInfo("Print functionality not yet implemented");
    }

    /**
     * Schedule report
     */
    @FXML
    private void scheduleReport() {
        logger.info("Schedule report clicked");
        showInfo("Schedule functionality not yet implemented");
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
