package com.inventory.service;

import com.inventory.dao.ProductDAO;
import com.inventory.dao.CategoryDAO;
import com.inventory.dao.SupplierDAO;
import com.inventory.model.Product;
import com.inventory.model.Category;
import com.inventory.model.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

/**
 * ProductService - Business logic for product operations
 */
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private final ProductDAO productDAO;
    private final CategoryDAO categoryDAO;
    private final SupplierDAO supplierDAO;
    private final StockService stockService;

    public ProductService() {
        this.productDAO = new ProductDAO();
        this.categoryDAO = new CategoryDAO();
        this.supplierDAO = new SupplierDAO();
        this.stockService = new StockService();
    }

    /**
     * Get all products
     */
    public List<Product> getAllProducts() throws SQLException {
        return productDAO.getAll();
    }

    /**
     * Get product by ID
     */
    public Product getProductById(int id) throws SQLException {
        return productDAO.getById(id);
    }

    /**
     * Add new product
     */
    public boolean addProduct(Product product) throws SQLException {
        // Validate product
        if (!validateProduct(product)) {
            logger.error("Product validation failed");
            return false;
        }

        // Check if SKU already exists
        if (productDAO.existsBySku(product.getSku())) {
            logger.error("Product with SKU {} already exists", product.getSku());
            throw new IllegalArgumentException("Product with this SKU already exists");
        }

        // Add product
        boolean success = productDAO.add(product);

        if (success) {
            logger.info("Product added successfully: {}", product.getName());

            // Check if stock is low and create alert if needed
            stockService.checkAndCreateLowStockAlert(product);
        }

        return success;
    }

    /**
     * Update existing product
     */
    public boolean updateProduct(Product product) throws SQLException {
        // Validate product
        if (!validateProduct(product)) {
            logger.error("Product validation failed");
            return false;
        }

        // Update product
        boolean success = productDAO.update(product);

        if (success) {
            logger.info("Product updated successfully: {}", product.getName());

            // Check if stock is low and create/update alert
            stockService.checkAndCreateLowStockAlert(product);
        }

        return success;
    }

    /**
     * Delete product
     */
    public boolean deleteProduct(int productId) throws SQLException {
        Product product = productDAO.getById(productId);

        if (product == null) {
            logger.error("Product not found with ID: {}", productId);
            return false;
        }

        boolean success = productDAO.delete(productId);

        if (success) {
            logger.info("Product deleted successfully: {}", product.getName());
        }

        return success;
    }

    /**
     * Search products by name or SKU
     */
    public List<Product> searchProducts(String searchTerm) throws SQLException {
        return productDAO.search(searchTerm);
    }

    /**
     * Get products by category
     */
    public List<Product> getProductsByCategory(int categoryId) throws SQLException {
        return productDAO.getByCategory(categoryId);
    }

    /**
     * Get products by supplier
     */
    public List<Product> getProductsBySupplier(int supplierId) throws SQLException {
        return productDAO.getBySupplier(supplierId);
    }

    /**
     * Get low stock products
     */
    public List<Product> getLowStockProducts(int threshold) throws SQLException {
        return productDAO.getLowStockProducts(threshold);
    }

    /**
     * Get all categories
     */
    public List<Category> getAllCategories() throws SQLException {
        return categoryDAO.getAll();
    }

    /**
     * Get all suppliers
     */
    public List<Supplier> getAllSuppliers() throws SQLException {
        return supplierDAO.getAll();
    }

    /**
     * Get total product count
     */
    public int getTotalProductCount() throws SQLException {
        return productDAO.getCount();
    }

    /**
     * Validate product data
     */
    private boolean validateProduct(Product product) {
        if (product == null) {
            logger.error("Product is null");
            return false;
        }

        if (product.getName() == null || product.getName().trim().isEmpty()) {
            logger.error("Product name is required");
            return false;
        }

        if (product.getSku() == null || product.getSku().trim().isEmpty()) {
            logger.error("Product SKU is required");
            return false;
        }

        if (product.getPrice() < 0) {
            logger.error("Product price cannot be negative");
            return false;
        }

        if (product.getQuantity() < 0) {
            logger.error("Product quantity cannot be negative");
            return false;
        }

        return true;
    }

    /**
     * Update product stock quantity
     */
    public boolean updateProductStock(int productId, int newQuantity) throws SQLException {
        Product product = productDAO.getById(productId);

        if (product == null) {
            logger.error("Product not found with ID: {}", productId);
            return false;
        }

        product.setQuantity(newQuantity);
        boolean success = productDAO.update(product);

        if (success) {
            logger.info("Product stock updated: {} - New quantity: {}", product.getName(), newQuantity);

            // Check and create low stock alert if needed
            stockService.checkAndCreateLowStockAlert(product);
        }

        return success;
    }
}
