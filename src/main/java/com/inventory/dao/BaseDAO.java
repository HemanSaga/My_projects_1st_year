package com.inventory.dao;

import com.inventory.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * BaseDAO - Base class for all DAO classes
 * Provides common database operations and resource management
 */
public abstract class BaseDAO {

    protected static final Logger logger = LoggerFactory.getLogger(BaseDAO.class);
    protected final DatabaseConnection dbConnection;

    /**
     * Constructor - initializes database connection
     */
    public BaseDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Get a database connection from the pool
     */
    protected Connection getConnection() throws SQLException {
        return dbConnection.getConnection();
    }

    /**
     * Release connection back to the pool
     */
    protected void releaseConnection(Connection conn) {
        if (conn != null) {
            dbConnection.releaseConnection(conn);
        }
    }

    /**
     * Close all database resources safely
     * Should be called in finally block
     */
    protected void closeResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
        try {
            if (rs != null && !rs.isClosed()) {
                rs.close();
                logger.debug("ResultSet closed");
            }
        } catch (SQLException e) {
            logger.error("Error closing ResultSet", e);
        }

        try {
            if (stmt != null && !stmt.isClosed()) {
                stmt.close();
                logger.debug("PreparedStatement closed");
            }
        } catch (SQLException e) {
            logger.error("Error closing PreparedStatement", e);
        }

        if (conn != null) {
            releaseConnection(conn);
            logger.debug("Connection released to pool");
        }
    }

    /**
     * Close resources without ResultSet
     */
    protected void closeResources(Connection conn, PreparedStatement stmt) {
        closeResources(conn, stmt, null);
    }

    /**
     * Execute a count query
     */
    protected int executeCountQuery(String sql, Object... params) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);

            // Set parameters
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            logger.error("Error executing count query", e);
        } finally {
            closeResources(conn, stmt, rs);
        }

        return 0;
    }

    /**
     * Execute an update/insert/delete query
     */
    protected int executeUpdate(String sql, Object... params) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);

            // Set parameters
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            return stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error executing update", e);
        } finally {
            closeResources(conn, stmt);
        }

        return 0;
    }

    /**
     * Check if a record exists
     */
    protected boolean exists(String sql, Object... params) {
        return executeCountQuery(sql, params) > 0;
    }

    /**
     * Begin transaction
     */
    protected void beginTransaction(Connection conn) throws SQLException {
        if (conn != null) {
            conn.setAutoCommit(false);
            logger.debug("Transaction started");
        }
    }

    /**
     * Commit transaction
     */
    protected void commitTransaction(Connection conn) throws SQLException {
        if (conn != null) {
            conn.commit();
            conn.setAutoCommit(true);
            logger.debug("Transaction committed");
        }
    }

    /**
     * Rollback transaction
     */
    protected void rollbackTransaction(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);
                logger.debug("Transaction rolled back");
            } catch (SQLException e) {
                logger.error("Error rolling back transaction", e);
            }
        }
    }
}
