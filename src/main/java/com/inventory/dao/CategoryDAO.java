package com.inventory.dao;

import com.inventory.model.Category;
import com.inventory.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CategoryDAO - Handles all category-related database operations
 */
public class CategoryDAO {

    private static final Logger logger = LoggerFactory.getLogger(CategoryDAO.class);
    private final DatabaseConnection dbConnection;

    public CategoryDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Create a new category
     */
    public boolean createCategory(Category category) {
        String sql = "INSERT INTO categories (category_name, description) VALUES (?, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, category.getCategoryName());
            stmt.setString(2, category.getDescription());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    category.setCategoryId(rs.getInt(1));
                }
                logger.info("Category created: {}", category.getCategoryName());
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error creating category", e);
        } finally {
            closeResources(conn, stmt, rs);
        }

        return false;
    }

    /**
     * Get all categories
     */
    public List<Category> getAllCategories() {
        String sql = "SELECT * FROM categories ORDER BY category_name";

        List<Category> categories = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                categories.add(extractCategoryFromResultSet(rs));
            }

        } catch (SQLException e) {
            logger.error("Error getting categories", e);
        } finally {
            closeResources(conn, stmt, rs);
        }

        return categories;
    }

    /**
     * Get category by ID
     */
    public Category getCategoryById(int categoryId) {
        String sql = "SELECT * FROM categories WHERE category_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, categoryId);

            rs = stmt.executeQuery();

            if (rs.next()) {
                return extractCategoryFromResultSet(rs);
            }

        } catch (SQLException e) {
            logger.error("Error getting category by ID", e);
        } finally {
            closeResources(conn, stmt, rs);
        }

        return null;
    }

    /**
     * Extract Category from ResultSet
     */
    private Category extractCategoryFromResultSet(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setCategoryId(rs.getInt("category_id"));
        category.setCategoryName(rs.getString("category_name"));
        category.setDescription(rs.getString("description"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            category.setCreatedAt(created.toLocalDateTime());
        }

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            category.setUpdatedAt(updated.toLocalDateTime());
        }

        return category;
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
     * Adapter: getAll() -> getAllCategories()
     */
    public List<Category> getAll() throws SQLException {
        return getAllCategories();
    }

    /**
     * Adapter: getById(int) -> getCategoryById(int)
     */
    public Category getById(int id) throws SQLException {
        return getCategoryById(id);
    }

    /**
     * Adapter: add(Category) -> createCategory(Category)
     */
    public boolean add(Category category) throws SQLException {
        return createCategory(category);
    }

    /**
     * Update category
     */
    public boolean update(Category category) throws SQLException {
        String sql = "UPDATE categories SET category_name = ?, description = ? WHERE category_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, category.getCategoryName());
            stmt.setString(2, category.getDescription());
            stmt.setInt(3, category.getCategoryId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating category", e);
            throw e;
        } finally {
            closeResources(conn, stmt, null);
        }
    }

    /**
     * Delete category
     */
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM categories WHERE category_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = dbConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting category", e);
            throw e;
        } finally {
            closeResources(conn, stmt, null);
        }
    }
}
