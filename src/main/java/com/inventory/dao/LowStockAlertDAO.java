package com.inventory.dao;

import com.inventory.model.LowStockAlert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * LowStockAlertDAO - Handles all low stock alert database operations
 */
public class LowStockAlertDAO extends BaseDAO {

    private static final Logger logger = LoggerFactory.getLogger(LowStockAlertDAO.class);

    /**
     * Get all pending low stock alerts
     */
    public List<LowStockAlert> getPendingAlerts() {
        String sql = "SELECT a.*, p.product_code, p.product_name " +
                "FROM low_stock_alerts a " +
                "JOIN products p ON a.product_id = p.product_id " +
                "WHERE a.status = 'pending' " +
                "ORDER BY a.alert_date DESC";

        List<LowStockAlert> alerts = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                alerts.add(extractAlertFromResultSet(rs));
            }

            logger.info("Retrieved {} pending alerts", alerts.size());

        } catch (SQLException e) {
            logger.error("Error getting pending alerts", e);
        } finally {
            closeResources(conn, stmt, rs);
        }

        return alerts;
    }

    /**
     * Get all alerts (any status)
     */
    public List<LowStockAlert> getAllAlerts() {
        String sql = "SELECT a.*, p.product_code, p.product_name " +
                "FROM low_stock_alerts a " +
                "JOIN products p ON a.product_id = p.product_id " +
                "ORDER BY a.alert_date DESC";

        List<LowStockAlert> alerts = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                alerts.add(extractAlertFromResultSet(rs));
            }

        } catch (SQLException e) {
            logger.error("Error getting all alerts", e);
        } finally {
            closeResources(conn, stmt, rs);
        }

        return alerts;
    }

    /**
     * Acknowledge an alert
     */
    public boolean acknowledgeAlert(int alertId, String acknowledgedBy) {
        String sql = "UPDATE low_stock_alerts SET status = 'acknowledged', " +
                "acknowledged_at = CURRENT_TIMESTAMP, acknowledged_by = ? " +
                "WHERE alert_id = ?";

        int rowsAffected = executeUpdate(sql, acknowledgedBy, alertId);

        if (rowsAffected > 0) {
            logger.info("Alert {} acknowledged by {}", alertId, acknowledgedBy);
            return true;
        }

        return false;
    }

    /**
     * Get alert count by status
     */
    public int getAlertCountByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM low_stock_alerts WHERE status = ?";
        return executeCountQuery(sql, status);
    }

    /**
     * Extract LowStockAlert from ResultSet
     */
    private LowStockAlert extractAlertFromResultSet(ResultSet rs) throws SQLException {
        LowStockAlert alert = new LowStockAlert();

        alert.setAlertId(rs.getInt("alert_id"));
        alert.setProductId(rs.getInt("product_id"));
        alert.setProductCode(rs.getString("product_code"));
        alert.setProductName(rs.getString("product_name"));
        alert.setCurrentStock(rs.getInt("current_stock"));
        alert.setReorderLevel(rs.getInt("reorder_level"));
        alert.setStatus(rs.getString("status"));
        alert.setAcknowledgedBy(rs.getString("acknowledged_by"));

        Timestamp alertDate = rs.getTimestamp("alert_date");
        if (alertDate != null) {
            alert.setAlertDate(alertDate.toLocalDateTime());
        }

        Timestamp acknowledgedAt = rs.getTimestamp("acknowledged_at");
        if (acknowledgedAt != null) {
            alert.setAcknowledgedAt(acknowledgedAt.toLocalDateTime());
        }

        return alert;
    }

    // ========== Adapter Methods for Service Layer Compatibility ==========

    /**
     * Get alerts by product ID
     */
    public List<LowStockAlert> getByProduct(int productId) throws SQLException {
        String sql = "SELECT a.*, p.product_code, p.product_name " +
                "FROM low_stock_alerts a " +
                "JOIN products p ON a.product_id = p.product_id " +
                "WHERE a.product_id = ? " +
                "ORDER BY a.alert_date DESC";

        List<LowStockAlert> alerts = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, productId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                alerts.add(extractAlertFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Error getting alerts by product", e);
            throw e;
        } finally {
            closeResources(conn, stmt, rs);
        }

        return alerts;
    }

    /**
     * Add new alert
     */
    public boolean add(LowStockAlert alert) throws SQLException {
        String sql = "INSERT INTO low_stock_alerts (product_id, current_stock, reorder_level, status) " +
                "VALUES (?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, alert.getProductId());
            stmt.setInt(2, alert.getCurrentStock());
            stmt.setInt(3, alert.getReorderLevel());
            stmt.setString(4, alert.getStatus());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    alert.setAlertId(rs.getInt(1));
                }
                logger.info("Low stock alert created for product {}", alert.getProductId());
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error adding alert", e);
            throw e;
        } finally {
            closeResources(conn, stmt, rs);
        }

        return false;
    }

    /**
     * Update alert
     */
    public boolean update(LowStockAlert alert) throws SQLException {
        String sql = "UPDATE low_stock_alerts SET current_stock = ?, reorder_level = ?, status = ? " +
                "WHERE alert_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, alert.getCurrentStock());
            stmt.setInt(2, alert.getReorderLevel());
            stmt.setString(3, alert.getStatus());
            stmt.setInt(4, alert.getAlertId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating alert", e);
            throw e;
        } finally {
            closeResources(conn, stmt, null);
        }
    }

    /**
     * Get active (pending) alerts
     */
    public List<LowStockAlert> getActiveAlerts() throws SQLException {
        return getPendingAlerts();
    }

    /**
     * Get alert by ID
     */
    public LowStockAlert getById(int id) throws SQLException {
        String sql = "SELECT a.*, p.product_code, p.product_name " +
                "FROM low_stock_alerts a " +
                "JOIN products p ON a.product_id = p.product_id " +
                "WHERE a.alert_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return extractAlertFromResultSet(rs);
            }
        } catch (SQLException e) {
            logger.error("Error getting alert by ID", e);
            throw e;
        } finally {
            closeResources(conn, stmt, rs);
        }

        return null;
    }
}
