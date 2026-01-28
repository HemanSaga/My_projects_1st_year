CREATE DATABASE IF NOT EXISTS inventory_management;
USE inventory_management;

CREATE TABLE categories (
    category_id INT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE suppliers (
    supplier_id INT PRIMARY KEY AUTO_INCREMENT,
    supplier_name VARCHAR(200) NOT NULL,
    contact_person VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE products (
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    product_code VARCHAR(50) NOT NULL UNIQUE,
    product_name VARCHAR(200) NOT NULL,
    description TEXT,
    category_id INT,
    supplier_id INT,
    unit_price DECIMAL(10,2) DEFAULT 0.00,
    quantity_in_stock INT DEFAULT 0,
    reorder_level INT DEFAULT 10,
    unit_of_measure VARCHAR(20) DEFAULT 'pcs',
    status VARCHAR(20) DEFAULT 'active',
    barcode VARCHAR(100),
    image_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE SET NULL,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id) ON DELETE SET NULL
);

CREATE TABLE stock_transactions (
    transaction_id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    reference_number VARCHAR(100),
    notes TEXT,
    performed_by VARCHAR(100),
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);

CREATE TABLE low_stock_alerts (
    alert_id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    current_stock INT NOT NULL,
    reorder_level INT NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',
    alert_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    acknowledged_at TIMESTAMP NULL,
    acknowledged_by VARCHAR(100),
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);

-- Insert sample data
INSERT INTO categories (category_name, description) VALUES
('Electronics', 'Electronic devices and accessories'),
('Furniture', 'Office and home furniture'),
('Stationery', 'Office supplies and stationery');

INSERT INTO suppliers (supplier_name, contact_person, email, phone, address) VALUES
('TechSupply Co.', 'John Smith', 'john@techsupply.com', '+1-555-0100', '123 Tech Street'),
('Furniture Depot', 'Jane Doe', 'jane@furnituredepot.com', '+1-555-0200', '456 Furniture Ave');

INSERT INTO products (product_code, product_name, description, category_id, supplier_id, unit_price, quantity_in_stock, reorder_level) VALUES
('ELEC001', 'Wireless Mouse', 'Ergonomic wireless mouse', 1, 1, 25.99, 50, 10),
('FURN001', 'Office Chair', 'Ergonomic office chair', 2, 2, 199.99, 15, 5),
('STAT001', 'A4 Paper Pack', '500 sheets per pack', 3, 1, 5.99, 100, 20);