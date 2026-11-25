-- ============================================================
-- RetailIQ: Realistic Sample Dataset for Training & Testing
-- ============================================================
USE retail_iq_db;

-- Safely clear previous data
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE forecast_logs;
TRUNCATE TABLE inventory_logs;
TRUNCATE TABLE sale_items;
TRUNCATE TABLE sales_transactions;
TRUNCATE TABLE products;
TRUNCATE TABLE categories;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. Insert Users
INSERT INTO users (user_id, username, password_hash, full_name, role) VALUES 
(1, 'admin', 'admin123', 'System Administrator', 'ADMIN'),
(2, 'manager', 'manager123', 'Store Inventory Manager', 'MANAGER');

-- 2. Insert Categories
INSERT INTO categories (category_id, category_name, description) VALUES 
(1, 'Electronics', 'Consumer electronics, accessories, and computing peripherals'),
(2, 'Groceries', 'Daily packaged foods, staples, and organic provisions'),
(3, 'Apparel', 'Men and women fashion, casuals, and seasonal wear'),
(4, 'Personal Care', 'Skin care, hair care, and personal grooming essentials');

-- 3. Insert Products
INSERT INTO products (product_id, category_id, sku, product_name, cost_price, unit_price, current_stock, safety_stock, lead_time_days) VALUES 
(1, 1, 'ELEC-MOU-01', 'Wireless Optical Mouse', 12.00, 24.99, 45, 15, 3),
(2, 1, 'ELEC-KEY-02', 'Mechanical Gaming Keyboard', 35.00, 69.99, 12, 10, 5),
(3, 2, 'GROC-MILK-01', 'Organic Almond Milk 1L', 2.10, 4.50, 85, 25, 2),
(4, 2, 'GROC-RICE-02', 'Premium Basmati Rice 5kg', 9.50, 16.99, 20, 15, 4),
(5, 3, 'APP-TSHIRT-01', 'Cotton Crew-Neck T-Shirt', 6.00, 18.00, 60, 20, 7),
(6, 4, 'PC-SHAMP-01', 'Herbal Anti-Dandruff Shampoo', 4.50, 9.99, 8, 12, 4);

-- Initial Inventory Log records for opening stock
INSERT INTO inventory_logs (product_id, user_id, change_type, quantity_changed, stock_after_change, reason) VALUES
(1, 1, 'PURCHASE_RESTOCK', 45, 45, 'Initial Opening Stock'),
(2, 1, 'PURCHASE_RESTOCK', 12, 12, 'Initial Opening Stock'),
(3, 1, 'PURCHASE_RESTOCK', 85, 85, 'Initial Opening Stock'),
(4, 1, 'PURCHASE_RESTOCK', 20, 20, 'Initial Opening Stock'),
(5, 1, 'PURCHASE_RESTOCK', 60, 60, 'Initial Opening Stock'),
(6, 1, 'PURCHASE_RESTOCK', 8, 8, 'Initial Opening Stock');

-- 4. Stored Procedure to generate 60 days of historical daily sales
DROP PROCEDURE IF EXISTS GenerateHistoricalSales;
DELIMITER //

CREATE PROCEDURE GenerateHistoricalSales()
BEGIN
    DECLARE day_offset INT DEFAULT 60;
    DECLARE cur_date DATE;
    DECLARE trans_id INT DEFAULT 1;
    DECLARE day_of_wk INT;
    DECLARE p1_qty INT;
    DECLARE p2_qty INT;
    DECLARE p3_qty INT;
    DECLARE p4_qty INT;
    DECLARE p5_qty INT;
    DECLARE p6_qty INT;
    DECLARE trans_total DECIMAL(12, 2);

    WHILE day_offset >= 1 DO
        SET cur_date = DATE_SUB(CURDATE(), INTERVAL day_offset DAY);
        SET day_of_wk = DAYOFWEEK(cur_date); -- 1=Sun, 7=Sat

        -- Realistic sales volume: higher on weekends (1 and 7)
        IF day_of_wk IN (1, 7) THEN
            SET p1_qty = FLOOR(8 + (RAND() * 8));   -- 8 to 15 units
            SET p2_qty = FLOOR(3 + (RAND() * 5));   -- 3 to 7 units
            SET p3_qty = FLOOR(15 + (RAND() * 12)); -- 15 to 26 units
            SET p4_qty = FLOOR(6 + (RAND() * 6));   -- 6 to 11 units
            SET p5_qty = FLOOR(7 + (RAND() * 8));   -- 7 to 14 units
            SET p6_qty = FLOOR(4 + (RAND() * 5));   -- 4 to 8 units
        ELSE
            SET p1_qty = FLOOR(4 + (RAND() * 5));   -- 4 to 8 units
            SET p2_qty = FLOOR(1 + (RAND() * 3));   -- 1 to 3 units
            SET p3_qty = FLOOR(10 + (RAND() * 8));  -- 10 to 17 units
            SET p4_qty = FLOOR(3 + (RAND() * 4));   -- 3 to 6 units
            SET p5_qty = FLOOR(3 + (RAND() * 5));   -- 3 to 7 units
            SET p6_qty = FLOOR(2 + (RAND() * 3));   -- 2 to 4 units
        END IF;

        SET trans_total = (p1_qty * 24.99) + (p2_qty * 69.99) + (p3_qty * 4.50) + 
                          (p4_qty * 16.99) + (p5_qty * 18.00) + (p6_qty * 9.99);

        -- Insert daily transaction header
        INSERT INTO sales_transactions (transaction_id, user_id, invoice_number, transaction_date, total_amount, payment_method) 
        VALUES (trans_id, 1, CONCAT('INV-', DATE_FORMAT(cur_date, '%Y%m%d'), '-001'), 
                CONCAT(cur_date, ' 18:30:00'), trans_total, 
                ELT(1 + FLOOR(RAND() * 3), 'CARD', 'UPI', 'CASH'));

        -- Insert line items
        INSERT INTO sale_items (transaction_id, product_id, quantity, unit_price, subtotal) VALUES
        (trans_id, 1, p1_qty, 24.99, p1_qty * 24.99),
        (trans_id, 2, p2_qty, 69.99, p2_qty * 69.99),
        (trans_id, 3, p3_qty, 4.50, p3_qty * 4.50),
        (trans_id, 4, p4_qty, 16.99, p4_qty * 16.99),
        (trans_id, 5, p5_qty, 18.00, p5_qty * 18.00),
        (trans_id, 6, p6_qty, 9.99, p6_qty * 9.99);

        SET trans_id = trans_id + 1;
        SET day_offset = day_offset - 1;
    END WHILE;
END //

DELIMITER ;

-- Execute the data generation procedure
CALL GenerateHistoricalSales();

-- Drop procedure after population to keep schema clean
DROP PROCEDURE IF EXISTS GenerateHistoricalSales;
