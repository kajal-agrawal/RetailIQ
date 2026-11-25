-- ============================================================
-- RetailIQ: Database Schema Definition (DDL)
-- ============================================================
CREATE DATABASE IF NOT EXISTS retail_iq_db
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE retail_iq_db;

-- Safely drop existing tables in correct order if recreating
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS forecast_logs;
DROP TABLE IF EXISTS inventory_logs;
DROP TABLE IF EXISTS sale_items;
DROP TABLE IF EXISTS sales_transactions;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. USERS TABLE (System Admins & Inventory Managers)
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'MANAGER') DEFAULT 'ADMIN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. CATEGORIES TABLE
CREATE TABLE categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 3. PRODUCTS TABLE
CREATE TABLE products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT NOT NULL,
    sku VARCHAR(50) NOT NULL UNIQUE,
    product_name VARCHAR(150) NOT NULL,
    cost_price DECIMAL(10, 2) NOT NULL CHECK (cost_price >= 0),
    unit_price DECIMAL(10, 2) NOT NULL CHECK (unit_price >= 0),
    current_stock INT NOT NULL DEFAULT 0 CHECK (current_stock >= 0),
    safety_stock INT NOT NULL DEFAULT 10 CHECK (safety_stock >= 0),
    lead_time_days INT NOT NULL DEFAULT 3 CHECK (lead_time_days > 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_category 
        FOREIGN KEY (category_id) REFERENCES categories(category_id) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_price_margin 
        CHECK (unit_price >= cost_price)
) ENGINE=InnoDB;

-- 4. SALES TRANSACTIONS (Order Headers)
CREATE TABLE sales_transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    payment_method ENUM('CASH', 'CARD', 'UPI') NOT NULL,
    CONSTRAINT fk_sales_user 
        FOREIGN KEY (user_id) REFERENCES users(user_id) 
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 5. SALE ITEMS (Order Line Items)
CREATE TABLE sale_items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10, 2) NOT NULL CHECK (unit_price >= 0),
    subtotal DECIMAL(10, 2) NOT NULL CHECK (subtotal >= 0),
    CONSTRAINT fk_items_transaction 
        FOREIGN KEY (transaction_id) REFERENCES sales_transactions(transaction_id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_items_product 
        FOREIGN KEY (product_id) REFERENCES products(product_id) 
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 6. INVENTORY LOGS (Audit Trail for stock movements)
CREATE TABLE inventory_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    user_id INT NOT NULL,
    change_type ENUM('PURCHASE_RESTOCK', 'SALE_DEDUCTION', 'MANUAL_ADJUSTMENT', 'DAMAGE') NOT NULL,
    quantity_changed INT NOT NULL,
    stock_after_change INT NOT NULL,
    reason VARCHAR(255),
    log_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inv_product 
        FOREIGN KEY (product_id) REFERENCES products(product_id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_inv_user 
        FOREIGN KEY (user_id) REFERENCES users(user_id) 
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 7. FORECAST & RECOMMENDATION AUDIT LOGS
CREATE TABLE forecast_logs (
    forecast_id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    model_used ENUM('LINEAR_REGRESSION', 'RANDOM_FOREST') NOT NULL,
    forecast_horizon_days INT NOT NULL DEFAULT 7,
    predicted_sales DECIMAL(10, 2) NOT NULL,
    calculated_safety_stock DECIMAL(10, 2) NOT NULL,
    recommended_reorder_qty INT NOT NULL,
    alert_status ENUM('CRITICAL_STOCKOUT', 'LOW_STOCK', 'OPTIMAL', 'OVERSTOCK') NOT NULL,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_forecast_product 
        FOREIGN KEY (product_id) REFERENCES products(product_id) 
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Indexes for performance
CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_sales_date ON sales_transactions(transaction_date);
CREATE INDEX idx_sale_items_product ON sale_items(product_id);
CREATE INDEX idx_inv_logs_prod_date ON inventory_logs(product_id, log_timestamp);
