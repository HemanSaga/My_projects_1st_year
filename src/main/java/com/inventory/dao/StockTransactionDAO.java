package com.inventory.dao;

import com.inventory.model.StockTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * StockTransactionDAO - Handles all stock transaction database operations
 */
public class StockTransactionDAO extends BaseDAO {

    private static final Logger logger = LoggerFactory.getLogger(StockTransactionDAO.class);

    /**
     * Create a new stock transaction
     */
    public boolean createTransaction(StockTransaction transaction) {
        String sql = "INSERT INTO stock_transactions (product_id, transaction_type, quantity, " +
                "reference_number, notes, performed_by) VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setInt(1, transaction.getProductId());
            stmt.setString(2, transaction.getTransactionType());
            stmt.setInt(3, transaction.getQuantity());
            stmt.setString(4, transaction.getReferenceNumber());
            stmt.setString(5, transaction.getNotes());
            stmt.setString(6, transaction.getPerformedBy());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    transaction.setTransactionId(rs.getInt(1));
                }
                logger.info("Stock transaction created: {}", transaction.getTransactionType());
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error creating stock transaction", e);
        } finally {
            closeResources(conn, stmt, rs);
        }

        return false;
    }

    /**
     * Record stock in transaction and update product quantity
     */
    public boolean recordStockIn(int productId, int quantity, String referenceNumber,
            String notes, String performedBy) {
        Connection conn = null;
        PreparedStatement updateStmt = null;
        PreparedStatement insertStmt = null;

        try {
            conn = getConnection();
            beginTransaction(conn);

            // Update product quantity
            String updateSql = "UPDATE products SET quantity_in_stock = quantity_in_stock + ? " +
                    "WHERE product_id = ?";
            updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setInt(1, quantity);
            updateStmt.setInt(2, productId);

            int updated = updateStmt.executeUpdate();

            if (updated > 0) {
                // Create transaction record
                String insertSql = "INSERT INTO stock_transactions " +
                        "(product_id, transaction_type, quantity, reference_number, notes, performed_by) " +
                        "VALUES (?, 'stock_in', ?, ?, ?, ?)";
                insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setInt(1, productId);
                insertStmt.setInt(2, quantity);
                insertStmt.setString(3, referenceNumber);
                insertStmt.setString(4, notes);
                insertStmt.setString(5, performedBy);

                insertStmt.executeUpdate();

                commitTransaction(conn);
                logger.info("Stock in recorded: Product={}, Quantity={}", productId, quantity);
                return true;
            }

            rollbackTransaction(conn);

        } catch (SQLException e) {
            logger.error("Error recording stock in", e);
            rollbackTransaction(conn);
        } finally {
            closeResources(null, updateStmt);
            closeResources(conn, insertStmt);
        }

        return false;
    }

    /**
     * Record stock out transaction and update product quantity
     */
    public boolean recordStockOut(int productId, int quantity, String referenceNumber,
            String notes, String performedBy) {
        Connection conn = null;
        PreparedStatement checkStmt = null;
        PreparedStatement updateStmt = null;
        PreparedStatement insertStmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            beginTransaction(conn);

            // Check current stock
            String checkSql = "SELECT quantity_in_stock FROM products WHERE product_id = ?";
            checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, productId);
            rs = checkStmt.executeQuery();

            if (rs.next()) {
                int currentStock = rs.getInt("quantity_in_stock");

                if (currentStock < quantity) {
                    logger.warn("Insufficient stock for product {}: available={}, requested={}",
                            productId, currentStock, quantity);
                    rollbackTransaction(conn);
                    return false;
                }

                // Update product quantity
                String updateSql = "UPDATE products SET quantity_in_stock = quantity_in_stock - ? " +
                        "WHERE product_id = ?";
                updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, quantity);
                updateStmt.setInt(2, productId);

                int updated = updateStmt.executeUpdate();

                if (updated > 0) {
                    // Create transaction record
                    String insertSql = "INSERT INTO stock_transactions " +
                            "(product_id, transaction_type, quantity, reference_number, notes, performed_by) " +
                            "VALUES (?, 'stock_out', ?, ?, ?, ?)";
                    insertStmt = conn.prepareStatement(insertSql);
                    insertStmt.setInt(1, productId);
                    insertStmt.setInt(2, quantity);
                    insertStmt.setString(3, referenceNumber);
                    insertStmt.setString(4, notes);
                    insertStmt.setString(5, performedBy);

                    insertStmt.executeUpdate();

                    commitTransaction(conn);
                    logger.info("Stock out recorded: Product={}, Quantity={}", productId, quantity);
                    return true;
                }
            }

            rollbackTransaction(conn);

        } catch (SQLException e) {
            logger.error("Error recording stock out", e);
            rollbackTransaction(conn);
        } finally {
            closeResources(null, checkStmt, rs);
            closeResources(null, updateStmt);
            closeResources(conn, insertStmt);
        }

        return false;
    }

    /**
     * Record stock adjustment (manual correction)
     */
    public boolean recordStockAdjustment(int productId, int newQuantity, String referenceNumber,
            String notes, String performedBy) {
        Connection conn = null;
        PreparedStatement updateStmt = null;
        PreparedStatement insertStmt = null;

        try {
            conn = getConnection();
            beginTransaction(conn);

            // Update product quantity directly
            String updateSql = "UPDATE products SET quantity_in_stock = ? WHERE product_id = ?";
            updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setInt(1, newQuantity);
            updateStmt.setInt(2, productId);

            int updated = updateStmt.executeUpdate();

            if (updated > 0) {
                // Create transaction record
                String insertSql = "INSERT INTO stock_transactions " +
                        "(product_id, transaction_type, quantity, reference_number, notes, performed_by) " +
                        "VALUES (?, 'adjustment', ?, ?, ?, ?)";
                insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setInt(1, productId);
                insertStmt.setInt(2, newQuantity);
                insertStmt.setString(3, referenceNumber);
                insertStmt.setString(4, notes);
                insertStmt.setString(5, performedBy);

                insertStmt.executeUpdate();

                commitTransaction(conn);
                logger.info("Stock adjustment recorded: Product={}, NewQuantity={}", productId, newQuantity);
                return true;
            }

            rollbackTransaction(conn);

        } catch (SQLException e) {
            logger.error("Error recording stock adjustment", e);
            rollbackTransaction(conn);
        } finally {
            closeResources(null, updateStmt);
            closeResources(conn, insertStmt);
        }

        return false;
    }

    /**
     * Get all transactions for a specific product
     */
    public List<StockTransaction> getTransactionsByProduct(int productId) {
        String sql = "SELECT st.*, p.product_code, p.product_name " +
                "FROM stock_transactions st " +
                "JOIN products p ON st.product_id = p.product_id " +
                "WHERE st.product_id = ? " +
                "ORDER BY st.transaction_date DESC";

        List<StockTransaction> transactions = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, productId);

            rs = stmt.executeQuery();

            while (rs.next()) {
                transactions.add(extractTransactionFromResultSet(rs));
            }

        } catch (SQLException e) {
            logger.error("Error getting transactions by product", e);
        } finally {
            closeResources(conn, stmt, rs);
        }

        return transactions;
    }

    /**
     * Get all transactions (recent first)
     */
    public List<StockTransaction> getAllTransactions(int limit) {
        String sql = "SELECT st.*, p.product_code, p.product_name " +
                "FROM stock_transactions st " +
                "JOIN products p ON st.product_id = p.product_id " +
                "ORDER BY st.transaction_date DESC " +
                "LIMIT ?";

        List<StockTransaction> transactions = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, limit);

            rs = stmt.executeQuery();

            while (rs.next()) {
                transactions.add(extractTransactionFromResultSet(rs));
            }

            logger.info("Retrieved {} transactions", transactions.size());

        } catch (SQLException e) {
            logger.error("Error getting all transactions", e);
        } finally {
            closeResources(conn, stmt, rs);
        }

        return transactions;
    }

    /**
     * Get transactions by type
     */
    public List<StockTransaction> getTransactionsByType(String transactionType, int limit) {
        String sql = "SELECT st.*, p.product_code, p.product_name " +
                "FROM stock_transactions st " +
                "JOIN products p ON st.product_id = p.product_id " +
                "WHERE st.transaction_type = ? " +
                "ORDER BY st.transaction_date DESC " +
                "LIMIT ?";

        List<StockTransaction> transactions = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, transactionType);
            stmt.setInt(2, limit);

            rs = stmt.executeQuery();

            while (rs.next()) {
                transactions.add(extractTransactionFromResultSet(rs));
            }

        } catch (SQLException e) {
            logger.error("Error getting transactions by type", e);
        } finally {
            closeResources(conn, stmt, rs);
        }

        return transactions;
    }

    /**
     * Get transaction statistics
     */
    public int getTotalStockInQuantity(int productId) {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM stock_transactions " +
                "WHERE product_id = ? AND transaction_type = 'stock_in'";
        return executeCountQuery(sql, productId);
    }

    public int getTotalStockOutQuantity(int productId) {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM stock_transactions " +
                "WHERE product_id = ? AND transaction_type = 'stock_out'";
        return executeCountQuery(sql, productId);
    }

    /**
     * Extract StockTransaction from ResultSet
     */
    private StockTransaction extractTransactionFromResultSet(ResultSet rs) throws SQLException {
        StockTransaction transaction = new StockTransaction();

        transaction.setTransactionId(rs.getInt("transaction_id"));
        transaction.setProductId(rs.getInt("product_id"));
        transaction.setProductCode(rs.getString("product_code"));
        transaction.setProductName(rs.getString("product_name"));
        transaction.setTransactionType(rs.getString("transaction_type"));
        transaction.setQuantity(rs.getInt("quantity"));
        transaction.setReferenceNumber(rs.getString("reference_number"));
        transaction.setNotes(rs.getString("notes"));
        transaction.setPerformedBy(rs.getString("performed_by"));

        Timestamp transactionDate = rs.getTimestamp("transaction_date");
        if (transactionDate != null) {
            transaction.setTransactionDate(transactionDate.toLocalDateTime());
        }

        return transaction;
    }

    // ========== Adapter Methods for Service Layer Compatibility ==========

    /**
     * Adapter: add(StockTransaction) -> createTransaction(StockTransaction)
     */
    public boolean add(StockTransaction transaction) throws SQLException {
        return createTransaction(transaction);
    }

    /**
     * Adapter: getAll() -> getAllTransactions(1000)
     */
    public List<StockTransaction> getAll() throws SQLException {
        return getAllTransactions(1000);
    }

    /**
     * Adapter: getByProduct(int) -> getTransactionsByProduct(int)
     */
    public List<StockTransaction> getByProduct(int productId) throws SQLException {
        return getTransactionsByProduct(productId);
    }

    /**
     * Adapter: getByType(String) -> getTransactionsByType(String, 1000)
     */
    public List<StockTransaction> getByType(String type) throws SQLException {
        return getTransactionsByType(type, 1000);
    }

    /**
     * Adapter: getRecent(int) -> getAllTransactions(int)
     */
    public List<StockTransaction> getRecent(int limit) throws SQLException {
        return getAllTransactions(limit);
    }

    /**
     * Get transactions by date range
     */
    public List<StockTransaction> getByDateRange(java.time.LocalDateTime startDate,
            java.time.LocalDateTime endDate) throws SQLException {
        String sql = "SELECT st.*, p.product_code, p.product_name " +
                "FROM stock_transactions st " +
                "JOIN products p ON st.product_id = p.product_id " +
                "WHERE st.transaction_date BETWEEN ? AND ? " +
                "ORDER BY st.transaction_date DESC";

        List<StockTransaction> transactions = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            rs = stmt.executeQuery();

            while (rs.next()) {
                transactions.add(extractTransactionFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Error getting transactions by date range", e);
            throw e;
        } finally {
            closeResources(conn, stmt, rs);
        }

        return transactions;
    }
}
