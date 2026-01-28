package com.inventory.service;

import com.inventory.dao.StockTransactionDAO;
import com.inventory.dao.ProductDAO;
import com.inventory.dao.LowStockAlertDAO;
import com.inventory.model.StockTransaction;
import com.inventory.model.Product;
import com.inventory.model.LowStockAlert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * StockService - Business logic for stock management
 * FIXED VERSION - Method name mismatches corrected
 */
public class StockService {

    private static final Logger logger = LoggerFactory.getLogger(StockService.class);
    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 10;

    private final StockTransactionDAO transactionDAO;
    private final ProductDAO productDAO;
    private final LowStockAlertDAO alertDAO;

    public StockService() {
        this.transactionDAO = new StockTransactionDAO();
        this.productDAO = new ProductDAO();
        this.alertDAO = new LowStockAlertDAO();
    }

    /**
     * Record stock in transaction
     */
    public boolean stockIn(int productId, int quantity, double unitPrice, String notes, String username)
            throws SQLException {

        if (quantity <= 0) {
            logger.error("Stock in quantity must be positive");
            return false;
        }

        Product product = productDAO.getById(productId);
        if (product == null) {
            logger.error("Product not found with ID: {}", productId);
            return false;
        }

        // Create transaction
        StockTransaction transaction = new StockTransaction();
        transaction.setProductId(productId);
        transaction.setTransactionType("stock_in");
        transaction.setQuantity(quantity);
        transaction.setUnitPrice(unitPrice);
        transaction.setTotalPrice(quantity * unitPrice);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setNotes(notes);
        transaction.setUsername(username);

        // Add transaction
        boolean success = transactionDAO.add(transaction);

        if (success) {
            // Update product quantity - FIXED: Using correct method name
            int newQuantity = product.getQuantityInStock() + quantity;
            product.setQuantityInStock(newQuantity);
            productDAO.update(product);

            logger.info("Stock in successful: {} - Quantity: {}", product.getProductName(), quantity);

            // Check and update/remove low stock alert
            checkAndCreateLowStockAlert(product);
        }

        return success;
    }

    /**
     * Record stock out transaction
     */
    public boolean stockOut(int productId, int quantity, double unitPrice, String notes, String username)
            throws SQLException {

        if (quantity <= 0) {
            logger.error("Stock out quantity must be positive");
            return false;
        }

        Product product = productDAO.getById(productId);
        if (product == null) {
            logger.error("Product not found with ID: {}", productId);
            return false;
        }

        // Check if sufficient stock available - FIXED
        if (product.getQuantityInStock() < quantity) {
            logger.error("Insufficient stock for product: {}. Available: {}, Requested: {}",
                    product.getProductName(), product.getQuantityInStock(), quantity);
            throw new IllegalArgumentException("Insufficient stock available");
        }

        // Create transaction
        StockTransaction transaction = new StockTransaction();
        transaction.setProductId(productId);
        transaction.setTransactionType("stock_out");
        transaction.setQuantity(quantity);
        transaction.setUnitPrice(unitPrice);
        transaction.setTotalPrice(quantity * unitPrice);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setNotes(notes);
        transaction.setUsername(username);

        // Add transaction
        boolean success = transactionDAO.add(transaction);

        if (success) {
            // Update product quantity - FIXED
            int newQuantity = product.getQuantityInStock() - quantity;
            product.setQuantityInStock(newQuantity);
            productDAO.update(product);

            logger.info("Stock out successful: {} - Quantity: {}", product.getProductName(), quantity);

            // Check and create low stock alert if needed
            checkAndCreateLowStockAlert(product);
        }

        return success;
    }

    /**
     * Get all transactions
     */
    public List<StockTransaction> getAllTransactions() throws SQLException {
        return transactionDAO.getAll();
    }

    /**
     * Get transactions by product
     */
    public List<StockTransaction> getTransactionsByProduct(int productId) throws SQLException {
        return transactionDAO.getByProduct(productId);
    }

    /**
     * Get transactions by date range
     */
    public List<StockTransaction> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate)
            throws SQLException {
        return transactionDAO.getByDateRange(startDate, endDate);
    }

    /**
     * Get transactions by type
     */
    public List<StockTransaction> getTransactionsByType(String type) throws SQLException {
        return transactionDAO.getByType(type);
    }

    /**
     * Get recent transactions
     */
    public List<StockTransaction> getRecentTransactions(int limit) throws SQLException {
        return transactionDAO.getRecent(limit);
    }

    /**
     * Calculate total stock value - FIXED
     */
    public double calculateTotalStockValue() throws SQLException {
        List<Product> products = productDAO.getAll();
        double totalValue = 0.0;

        for (Product product : products) {
            // FIXED: Using correct property access
            double productValue = product.getUnitPrice().doubleValue() * product.getQuantityInStock();
            totalValue += productValue;
        }

        return totalValue;
    }

    /**
     * Get transaction count for today
     */
    public int getTodayTransactionCount() throws SQLException {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        List<StockTransaction> todayTransactions = transactionDAO.getByDateRange(startOfDay, endOfDay);
        return todayTransactions.size();
    }

    /**
     * Check and create/update low stock alert - FIXED
     */
    public void checkAndCreateLowStockAlert(Product product) throws SQLException {
        // FIXED: Using correct method names
        int threshold = product.getReorderLevel() > 0 ? product.getReorderLevel() : DEFAULT_LOW_STOCK_THRESHOLD;

        // Check if alert already exists
        List<LowStockAlert> existingAlerts = alertDAO.getByProduct(product.getProductId());
        LowStockAlert existingAlert = existingAlerts.isEmpty() ? null : existingAlerts.get(0);

        if (product.getQuantityInStock() <= threshold) {
            // Stock is low - create or update alert
            if (existingAlert == null) {
                // Create new alert
                LowStockAlert alert = new LowStockAlert();
                alert.setProductId(product.getProductId());
                alert.setCurrentStock(product.getQuantityInStock());
                alert.setMinStockLevel(threshold);
                alert.setAlertDate(LocalDateTime.now());
                alert.setResolved(false);
                alert.setStatus("pending");

                alertDAO.add(alert);
                logger.info("Low stock alert created for product: {}", product.getProductName());
            } else if (!existingAlert.isResolved()) {
                // Update existing alert
                existingAlert.setCurrentStock(product.getQuantityInStock());
                existingAlert.setAlertDate(LocalDateTime.now());
                alertDAO.update(existingAlert);
                logger.info("Low stock alert updated for product: {}", product.getProductName());
            }
        } else {
            // Stock is sufficient - resolve alert if exists
            if (existingAlert != null && !existingAlert.isResolved()) {
                existingAlert.setResolved(true);
                existingAlert.setResolvedDate(LocalDateTime.now());
                existingAlert.setStatus("resolved");
                alertDAO.update(existingAlert);
                logger.info("Low stock alert resolved for product: {}", product.getProductName());
            }
        }
    }

    /**
     * Get all active low stock alerts
     */
    public List<LowStockAlert> getActiveLowStockAlerts() throws SQLException {
        return alertDAO.getActiveAlerts();
    }

    /**
     * Get low stock alert count
     */
    public int getLowStockAlertCount() throws SQLException {
        return alertDAO.getActiveAlerts().size();
    }

    /**
     * Resolve low stock alert
     */
    public boolean resolveLowStockAlert(int alertId) throws SQLException {
        LowStockAlert alert = alertDAO.getById(alertId);

        if (alert == null) {
            logger.error("Alert not found with ID: {}", alertId);
            return false;
        }

        alert.setResolved(true);
        alert.setResolvedDate(LocalDateTime.now());
        alert.setStatus("resolved");

        boolean success = alertDAO.update(alert);

        if (success) {
            logger.info("Low stock alert resolved: {}", alertId);
        }

        return success;
    }
}