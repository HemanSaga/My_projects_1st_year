package com.inventory.dao;

import com.inventory.model.Supplier;
import com.inventory.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SupplierDAO - Handles all supplier-related database operations
 */
public class SupplierDAO {

    private static final Logger logger = LoggerFactory.getLogger(SupplierDAO.class);
    private final DatabaseConnection dbConnection;

    public SupplierDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Create a new supplier
     */
    public boolean createSupplier(Supplier supplier) {
        String sql = "INSERT INTO suppliers (supplier_name, contact_person, email, phone, address) " +
                "VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, supplier.getSupplierName());
            stmt.setString(2, supplier.getContactPerson());
            stmt.setString(3, supplier.getEmail());
            stmt.setString(4, supplier.getPhone());
            stmt.setString(5, supplier.getAddress());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    supplier.setSupplierId(rs.getInt(1));
                }
                logger.info("Supplier created: {}", supplier.getSupplierName());
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error creating supplier", e);
        } finally {
            closeResources(conn, stmt, rs);
        }

        return false;
    }

    /**
     * Get all suppliers
     */
    public List<Supplier> getAllSuppliers() {
        String sql = "SELECT * FROM suppliers ORDER BY supplier_name";

        List<Supplier> suppliers = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                suppliers.add(extractSupplierFromResultSet(rs));
            }

        } catch (SQLException e) {
            logger.error("Error getting suppliers", e);
        } finally {
            closeResources(conn, stmt, rs);
        }

        return suppliers;
    }

    /**
     * Extract Supplier from ResultSet
     */
    private Supplier extractSupplierFromResultSet(ResultSet rs) throws SQLException {
        Supplier supplier = new Supplier();
        supplier.setSupplierId(rs.getInt("supplier_id"));
        supplier.setSupplierName(rs.getString("supplier_name"));
        supplier.setContactPerson(rs.getString("contact_person"));
        supplier.setEmail(rs.getString("email"));
        supplier.setPhone(rs.getString("phone"));
        supplier.setAddress(rs.getString("address"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            supplier.setCreatedAt(created.toLocalDateTime());
        }

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            supplier.setUpdatedAt(updated.toLocalDateTime());
        }

        return supplier;
    }

    /**
     * Close resources
     */
    private void closeResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
        try {
            if (rs != null)
                rs.close();
            if (stmt != null)
                stmt.close();
            if (conn != null)
                dbConnection.releaseConnection(conn);
        } catch (SQLException e) {
            logger.error("Error closing resources", e);
        }
    }

    // ========== Adapter Methods for Service Layer Compatibility ==========

    /**
     * Adapter: getAll() -> getAllSuppliers()
     */
    public List<Supplier> getAll() throws SQLException {
        return getAllSuppliers();
    }

    /**
     * Adapter: getById(int) -> getSupplierById(int)
     */
    public Supplier getById(int id) throws SQLException {
        String sql = "SELECT * FROM suppliers WHERE supplier_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return extractSupplierFromResultSet(rs);
            }
        } catch (SQLException e) {
            logger.error("Error getting supplier by ID", e);
            throw e;
        } finally {
            closeResources(conn, stmt, rs);
        }

        return null;
    }

    /**
     * Adapter: add(Supplier) -> createSupplier(Supplier)
     */
    public boolean add(Supplier supplier) throws SQLException {
        return createSupplier(supplier);
    }

    /**
     * Update supplier
     */
    public boolean update(Supplier supplier) throws SQLException {
        String sql = "UPDATE suppliers SET supplier_name = ?, contact_person = ?, email = ?, phone = ?, address = ? WHERE supplier_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, supplier.getSupplierName());
            stmt.setString(2, supplier.getContactPerson());
            stmt.setString(3, supplier.getEmail());
            stmt.setString(4, supplier.getPhone());
            stmt.setString(5, supplier.getAddress());
            stmt.setInt(6, supplier.getSupplierId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating supplier", e);
            throw e;
        } finally {
            closeResources(conn, stmt, null);
        }
    }

    /**
     * Delete supplier
     */
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM suppliers WHERE supplier_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting supplier", e);
            throw e;
        } finally {
            closeResources(conn, stmt, null);
        }
    }
}
