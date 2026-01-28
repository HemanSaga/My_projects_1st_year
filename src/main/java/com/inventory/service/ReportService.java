package com.inventory.service;

import com.inventory.dao.*;
import com.inventory.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ReportService - Business logic for report generation and analytics
 * FIXED VERSION - Method name mismatches corrected
 */
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    private final ProductDAO productDAO;
    private final StockTransactionDAO transactionDAO;
    private final CategoryDAO categoryDAO;
    private final SupplierDAO supplierDAO;

    public ReportService() {
        this.productDAO = new ProductDAO();
        this.transactionDAO = new StockTransactionDAO();
        this.categoryDAO = new CategoryDAO();
        this.supplierDAO = new SupplierDAO();
    }

    /**
     * Generate sales report for date range
     */
    public Map<String, Object> generateSalesReport(LocalDateTime startDate, LocalDateTime endDate)
            throws SQLException {

        List<StockTransaction> transactions = transactionDAO.getByDateRange(startDate, endDate);
        List<StockTransaction> outTransactions = transactions.stream()
                .filter(t -> "stock_out".equalsIgnoreCase(t.getTransactionType()) ||
                        "OUT".equalsIgnoreCase(t.getTransactionType()))
                .collect(Collectors.toList());

        Map<String, Object> report = new HashMap<>();

        // Calculate total revenue
        double totalRevenue = outTransactions.stream()
                .mapToDouble(StockTransaction::getTotalPrice)
                .sum();

        // Calculate total transactions
        int totalTransactions = outTransactions.size();

        // Calculate average transaction value
        double avgTransaction = totalTransactions > 0 ? totalRevenue / totalTransactions : 0.0;

        report.put("totalRevenue", totalRevenue);
        report.put("totalTransactions", totalTransactions);
        report.put("avgTransaction", avgTransaction);
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("transactions", outTransactions);

        logger.info("Sales report generated for period: {} to {}", startDate, endDate);

        return report;
    }

    /**
     * Generate stock report - FIXED
     */
    public Map<String, Object> generateStockReport() throws SQLException {
        List<Product> products = productDAO.getAll();

        Map<String, Object> report = new HashMap<>();

        // Calculate total stock value - FIXED
        double totalStockValue = products.stream()
                .mapToDouble(p -> p.getUnitPrice().doubleValue() * p.getQuantityInStock())
                .sum();

        // Count products
        int totalProducts = products.size();

        // Count low stock products - FIXED
        long lowStockCount = products.stream()
                .filter(p -> p.getQuantityInStock() <= p.getReorderLevel())
                .count();

        // Count out of stock products - FIXED
        long outOfStockCount = products.stream()
                .filter(p -> p.getQuantityInStock() == 0)
                .count();

        report.put("totalStockValue", totalStockValue);
        report.put("totalProducts", totalProducts);
        report.put("lowStockCount", lowStockCount);
        report.put("outOfStockCount", outOfStockCount);
        report.put("products", products);

        logger.info("Stock report generated");

        return report;
    }

    /**
     * Get products by category distribution - FIXED
     */
    public Map<String, Integer> getProductsByCategoryDistribution() throws SQLException {
        List<Product> products = productDAO.getAll();
        List<Category> categories = categoryDAO.getAll();

        Map<String, Integer> distribution = new HashMap<>();

        for (Category category : categories) {
            long count = products.stream()
                    .filter(p -> p.getCategoryId() == category.getCategoryId())
                    .count();
            distribution.put(category.getCategoryName(), (int) count);
        }

        return distribution;
    }

    /**
     * Get top selling products - FIXED
     */
    public List<Map<String, Object>> getTopSellingProducts(int limit, LocalDateTime startDate, LocalDateTime endDate)
            throws SQLException {

        List<StockTransaction> outTransactions = transactionDAO.getByDateRange(startDate, endDate).stream()
                .filter(t -> "stock_out".equalsIgnoreCase(t.getTransactionType()) ||
                        "OUT".equalsIgnoreCase(t.getTransactionType()))
                .collect(Collectors.toList());

        // Group by product and sum quantities
        Map<Integer, Integer> productQuantities = new HashMap<>();
        Map<Integer, Double> productRevenues = new HashMap<>();

        for (StockTransaction transaction : outTransactions) {
            int productId = transaction.getProductId();
            productQuantities.put(productId,
                    productQuantities.getOrDefault(productId, 0) + transaction.getQuantity());
            productRevenues.put(productId,
                    productRevenues.getOrDefault(productId, 0.0) + transaction.getTotalPrice());
        }

        // Create result list
        List<Map<String, Object>> topProducts = new ArrayList<>();

        for (Map.Entry<Integer, Integer> entry : productQuantities.entrySet()) {
            Product product = productDAO.getById(entry.getKey());
            if (product != null) {
                Map<String, Object> productData = new HashMap<>();
                productData.put("product", product);
                productData.put("quantitySold", entry.getValue());
                productData.put("revenue", productRevenues.get(entry.getKey()));
                topProducts.add(productData);
            }
        }

        // Sort by quantity sold and limit
        topProducts.sort((a, b) -> Integer.compare((Integer) b.get("quantitySold"), (Integer) a.get("quantitySold")));

        return topProducts.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * Get transaction trends (daily aggregation) - FIXED
     */
    public Map<String, Map<String, Integer>> getTransactionTrends(LocalDateTime startDate, LocalDateTime endDate)
            throws SQLException {

        List<StockTransaction> transactions = transactionDAO.getByDateRange(startDate, endDate);

        Map<String, Map<String, Integer>> trends = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Initialize all dates in range
        LocalDateTime current = startDate;
        while (!current.isAfter(endDate)) {
            String dateKey = current.format(formatter);
            Map<String, Integer> dayData = new HashMap<>();
            dayData.put("IN", 0);
            dayData.put("OUT", 0);
            trends.put(dateKey, dayData);
            current = current.plusDays(1);
        }

        // Aggregate transactions by date - FIXED to handle both naming conventions
        for (StockTransaction transaction : transactions) {
            String dateKey = transaction.getTransactionDate().format(formatter);
            if (trends.containsKey(dateKey)) {
                Map<String, Integer> dayData = trends.get(dateKey);
                String type = transaction.getTransactionType();

                // Normalize transaction type
                String normalizedType;
                if ("stock_in".equalsIgnoreCase(type)) {
                    normalizedType = "IN";
                } else if ("stock_out".equalsIgnoreCase(type)) {
                    normalizedType = "OUT";
                } else {
                    normalizedType = type.toUpperCase();
                }

                dayData.put(normalizedType, dayData.getOrDefault(normalizedType, 0) + transaction.getQuantity());
            }
        }

        return trends;
    }

    /**
     * Generate supplier performance report - FIXED
     */
    public List<Map<String, Object>> generateSupplierPerformanceReport() throws SQLException {
        List<Supplier> suppliers = supplierDAO.getAll();
        List<Product> products = productDAO.getAll();

        List<Map<String, Object>> performanceData = new ArrayList<>();

        for (Supplier supplier : suppliers) {
            Map<String, Object> data = new HashMap<>();

            // Count products from this supplier - FIXED
            long productCount = products.stream()
                    .filter(p -> p.getSupplierId() == supplier.getSupplierId())
                    .count();

            // Calculate total value of products from this supplier - FIXED
            double totalValue = products.stream()
                    .filter(p -> p.getSupplierId() == supplier.getSupplierId())
                    .mapToDouble(p -> p.getUnitPrice().doubleValue() * p.getQuantityInStock())
                    .sum();

            data.put("supplier", supplier);
            data.put("productCount", productCount);
            data.put("totalValue", totalValue);

            performanceData.add(data);
        }

        // Sort by total value
        performanceData.sort((a, b) -> Double.compare((Double) b.get("totalValue"), (Double) a.get("totalValue")));

        return performanceData;
    }

    /**
     * Get low stock products report
     */
    public List<Product> getLowStockProductsReport(int threshold) throws SQLException {
        return productDAO.getLowStockProducts(threshold);
    }

    /**
     * Calculate inventory turnover ratio - FIXED
     */
    public double calculateInventoryTurnover(LocalDateTime startDate, LocalDateTime endDate)
            throws SQLException {

        // Get total cost of goods sold (OUT transactions)
        List<StockTransaction> outTransactions = transactionDAO.getByDateRange(startDate, endDate).stream()
                .filter(t -> "stock_out".equalsIgnoreCase(t.getTransactionType()) ||
                        "OUT".equalsIgnoreCase(t.getTransactionType()))
                .collect(Collectors.toList());

        double cogs = outTransactions.stream()
                .mapToDouble(StockTransaction::getTotalPrice)
                .sum();

        // Get average inventory value - FIXED
        List<Product> products = productDAO.getAll();
        double avgInventoryValue = products.stream()
                .mapToDouble(p -> p.getUnitPrice().doubleValue() * p.getQuantityInStock())
                .average()
                .orElse(0.0);

        // Calculate turnover ratio
        return avgInventoryValue > 0 ? cogs / avgInventoryValue : 0.0;
    }

    /**
     * Export report data to CSV format
     */
    public String exportToCSV(List<Map<String, Object>> data, List<String> headers) {
        StringBuilder csv = new StringBuilder();

        // Add headers
        csv.append(String.join(",", headers)).append("\n");

        // Add data rows
        for (Map<String, Object> row : data) {
            List<String> values = new ArrayList<>();
            for (String header : headers) {
                Object value = row.get(header);
                values.add(value != null ? value.toString() : "");
            }
            csv.append(String.join(",", values)).append("\n");
        }

        return csv.toString();
    }
}