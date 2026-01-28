package com.inventory.dao;

import com.inventory.model.Product;
import com.inventory.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ProductDAO - Handles all product-related database operations
 */
public class ProductDAO {

    private static final Logger logger = LoggerFactory.getLogger(ProductDAO.class);
    private final DatabaseConnection dbConnection;

    public ProductDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Create a new product in the database
     * Checks for duplicate product codes
     */
    public boolean createProduct(Product product) {
        String sql = "INSERT INTO products (product_code, product_name, description, " +
                "category_id, supplier_id, unit_price, quantity_in_stock, reorder_level, " +
                "unit_of_measure, status, barcode, image_url) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Check for duplicate product code
            if (isProductCodeExists(product.getProductCode())) {
                logger.warn("Duplicate product code: {}", product.getProductCode());
                return false;
            }

            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, product.getProductCode());
            stmt.setString(2, product.getProductName());
            stmt.setString(3, product.getDescription());
            stmt.setObject(4, product.getCategoryId() > 0 ? product.getCategoryId() : null);
            stmt.setObject(5, product.getSupplierId() > 0 ? product.getSupplierId() : null);
            stmt.setBigDecimal(6, product.getUnitPrice());
            stmt.setInt(7, product.getQuantityInStock());
            stmt.setInt(8, product.getReorderLevel());
            stmt.setString(9, product.getUnitOfMeasure());
            stmt.setString(10, product.getStatus());
            stmt.setString(11, product.getBarcode());
            stmt.setString(12, product.getImageUrl());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Get the generated product ID
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    product.setProductId(rs.getInt(1));
                }
                logger.info("Product created successfully: {}", product.getProductCode());
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error creating product", e);
        } finally {
            closeResources(conn, stmt, rs);
        }

        return false;
    }

    /**
     * Update an existing product
     */
    public boolean updateProduct(Product product) {
        String sql = "UPDATE products SET product_name = ?, description = ?, " +
                "category_id = ?, supplier_id = ?, unit_price = ?, quantity_in_stock = ?, " +
                "reorder_level = ?, unit_of_measure = ?, status = ?, barcode = ?, " +
                "image_url = ? WHERE product_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql);

            stmt.setString(1, product.getProductName());
            stmt.setString(2, product.getDescription());
            stmt.setObject(3, product.getCategoryId() > 0 ? product.getCategoryId() : null);
            stmt.setObject(4, product.getSupplierId() > 0 ? product.getSupplierId() : null);
            stmt.setBigDecimal(5, product.getUnitPrice());
            stmt.setInt(6, product.getQuantityInStock());
            stmt.setInt(7, product.getReorderLevel());
            stmt.setString(8, product.getUnitOfMeasure());
            stmt.setString(9, product.getStatus());
            stmt.setString(10, product.getBarcode());
            stmt.setString(11, product.getImageUrl());
            stmt.setInt(12, product.getProductId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Product updated successfully: {}", product.getProductId());
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error updating product", e);
        } finally {
            closeResources(conn, stmt, null);
        }

        return false;
    }

    /**
     * Delete a product by ID
     */
    public boolean deleteProduct(int productId) {
        String sql = "DELETE FROM products WHERE product_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, productId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Product deleted successfully: {}", productId);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error deleting product", e);
        } finally {
            closeResources(conn, stmt, null);
        }

        return false;
    }

    /**
     * Get product by ID
     */
    public Product getProductById(int productId) {
        String sql = "SELECT p.*, c.category_name, s.supplier_name " +
                "FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.category_id " +
                "LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id " +
                "WHERE p.product_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, productId);

            rs = stmt.executeQuery();

            if (rs.next()) {
                return extractProductFromResultSet(rs);
            }

        } catch (SQLException e) {
            logger.error("Error getting product by ID", e);
        } finally {
            closeResources(conn, stmt, rs);
        }

        return null;
    }

    /**
     * Get all products
     */
    public List<Product> getAllProducts() {
        String sql = "SELECT p.*, c.category_name, s.supplier_name " +
                "FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.category_id " +
                "LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id " +
                "ORDER BY p.product_name";

        List<Product> products = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }

            logger.info("Retrieved {} products", products.size());

        } catch (SQLException e) {
            logger.error("Error getting all products", e);
        } finally {
            closeResources(conn, stmt, rs);
        }

        return products;
    }

    /**
     * Search products by multiple criteria
     */
    public List<Product> searchProducts(String searchTerm, Integer categoryId, String status) {
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, c.category_name, s.supplier_name " +
                        "FROM products p " +
                        "LEFT JOIN categories c ON p.category_id = c.category_id " +
                        "LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id " +
                        "WHERE 1=1 ");

        List<Object> parameters = new ArrayList<>();

        // Add search term filter
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sql.append("AND (p.product_code LIKE ? OR p.product_name LIKE ? OR p.description LIKE ?) ");
            String searchPattern = "%" + searchTerm + "%";
            parameters.add(searchPattern);
            parameters.add(searchPattern);
            parameters.add(searchPattern);
        }

        // Add category filter
        if (categoryId != null && categoryId > 0) {
            sql.append("AND p.category_id = ? ");
            parameters.add(categoryId);
        }

        // Add status filter
        if (status != null && !status.trim().isEmpty() && !status.equals("all")) {
            sql.append("AND p.status = ? ");
            parameters.add(status);
        }

        sql.append("ORDER BY p.product_name");

        List<Product> products = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql.toString());

            // Set parameters
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }

            rs = stmt.executeQuery();

            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }

            logger.info("Search found {} products", products.size());

        } catch (SQLException e) {
            logger.error("Error searching products", e);
        } finally {
            closeResources(conn, stmt, rs);
        }

        return products;
    }

    /**
     * Get products with low stock (below reorder level)
     */
    public List<Product> getLowStockProducts() {
        String sql = "SELECT p.*, c.category_name, s.supplier_name " +
                "FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.category_id " +
                "LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id " +
                "WHERE p.quantity_in_stock < p.reorder_level " +
                "AND p.status = 'active' " +
                "ORDER BY (p.reorder_level - p.quantity_in_stock) DESC";

        List<Product> products = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }

            logger.info("Found {} low stock products", products.size());

        } catch (SQLException e) {
            logger.error("Error getting low stock products", e);
        } finally {
            closeResources(conn, stmt, rs);
        }

        return products;
    }

    /**
     * Update product stock quantity
     */
    public boolean updateStock(int productId, int newQuantity) {
        String sql = "UPDATE products SET quantity_in_stock = ? WHERE product_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, newQuantity);
            stmt.setInt(2, productId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Stock updated for product {}: new quantity = {}", productId, newQuantity);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error updating stock", e);
        } finally {
            closeResources(conn, stmt, null);
        }

        return false;
    }

    /**
     * Check if product code already exists (for duplicate prevention)
     */
    public boolean isProductCodeExists(String productCode) {
        String sql = "SELECT COUNT(*) FROM products WHERE product_code = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, productCode);

            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            logger.error("Error checking product code", e);
        } finally {
            closeResources(conn, stmt, rs);
        }

        return false;
    }

    /**
     * Get total inventory value
     */
    public BigDecimal getTotalInventoryValue() {
        String sql = "SELECT SUM(unit_price * quantity_in_stock) FROM products WHERE status = 'active'";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            if (rs.next()) {
                BigDecimal total = rs.getBigDecimal(1);
                return total != null ? total : BigDecimal.ZERO;
            }

        } catch (SQLException e) {
            logger.error("Error calculating total inventory value", e);
        } finally {
            closeResources(conn, stmt, rs);
        }

        return BigDecimal.ZERO;
    }

    /**
     * Extract Product object from ResultSet
     */
    private Product extractProductFromResultSet(ResultSet rs) throws SQLException {
        Product product = new Product();

        product.setProductId(rs.getInt("product_id"));
        product.setProductCode(rs.getString("product_code"));
        product.setProductName(rs.getString("product_name"));
        product.setDescription(rs.getString("description"));
        product.setCategoryId(rs.getInt("category_id"));
        product.setCategoryName(rs.getString("category_name"));
        product.setSupplierId(rs.getInt("supplier_id"));
        product.setSupplierName(rs.getString("supplier_name"));
        product.setUnitPrice(rs.getBigDecimal("unit_price"));
        product.setQuantityInStock(rs.getInt("quantity_in_stock"));
        product.setReorderLevel(rs.getInt("reorder_level"));
        product.setUnitOfMeasure(rs.getString("unit_of_measure"));
        product.setStatus(rs.getString("status"));
        product.setBarcode(rs.getString("barcode"));
        product.setImageUrl(rs.getString("image_url"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            product.setCreatedAt(created.toLocalDateTime());
        }

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            product.setUpdatedAt(updated.toLocalDateTime());
        }

        return product;
    }

    /**
     * Close database resources
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
     * Adapter: getAll() -> getAllProducts()
     */
    public List<Product> getAll() throws SQLException {
        return getAllProducts();
    }

    /**
     * Adapter: getById(int) -> getProductById(int)
     */
    public Product getById(int id) throws SQLException {
        return getProductById(id);
    }

    /**
     * Adapter: add(Product) -> createProduct(Product)
     */
    public boolean add(Product product) throws SQLException {
        return createProduct(product);
    }

    /**
     * Adapter: update(Product) -> updateProduct(Product)
     */
    public boolean update(Product product) throws SQLException {
        return updateProduct(product);
    }

    /**
     * Adapter: delete(int) -> deleteProduct(int)
     */
    public boolean delete(int id) throws SQLException {
        return deleteProduct(id);
    }

    /**
     * Adapter: search(String) -> searchProducts(String, null, null)
     */
    public List<Product> search(String searchTerm) throws SQLException {
        return searchProducts(searchTerm, null, null);
    }

    /**
     * Adapter: getByCategory(int) -> searchProducts(null, categoryId, null)
     */
    public List<Product> getByCategory(int categoryId) throws SQLException {
        return searchProducts(null, categoryId, null);
    }

    /**
     * Adapter: getBySupplier(int) -> searchProducts with supplier filter
     */
    public List<Product> getBySupplier(int supplierId) throws SQLException {
        String sql = "SELECT p.*, c.category_name, s.supplier_name " +
                "FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.category_id " +
                "LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id " +
                "WHERE p.supplier_id = ? " +
                "ORDER BY p.product_name";

        List<Product> products = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, supplierId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Error getting products by supplier", e);
            throw e;
        } finally {
            closeResources(conn, stmt, rs);
        }

        return products;
    }

    /**
     * Adapter: getLowStockProducts(int) -> getLowStockProducts() with threshold
     * filter
     */
    public List<Product> getLowStockProducts(int threshold) throws SQLException {
        String sql = "SELECT p.*, c.category_name, s.supplier_name " +
                "FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.category_id " +
                "LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id " +
                "WHERE p.quantity_in_stock <= ? " +
                "AND p.status = 'active' " +
                "ORDER BY p.quantity_in_stock ASC";

        List<Product> products = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, threshold);
            rs = stmt.executeQuery();

            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Error getting low stock products", e);
            throw e;
        } finally {
            closeResources(conn, stmt, rs);
        }

        return products;
    }

    /**
     * Get total product count
     */
    public int getCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE status = 'active'";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error getting product count", e);
            throw e;
        } finally {
            closeResources(conn, stmt, rs);
        }

        return 0;
    }

    /**
     * Check if SKU exists
     */
    public boolean existsBySku(String sku) throws SQLException {
        return isProductCodeExists(sku);
    }

    /**
     * Get count of products in a specific category
     */
    public int getProductCountByCategory(int categoryId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE category_id = ? AND status = 'active'";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, categoryId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error getting product count by category", e);
            throw e;
        } finally {
            closeResources(conn, stmt, rs);
        }

        return 0;
    }
}
