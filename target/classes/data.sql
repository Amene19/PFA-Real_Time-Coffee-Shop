-- Roles
INSERT IGNORE INTO roles (name) VALUES 
('ROLE_ADMIN'),
('ROLE_SERVER'),
('ROLE_BARMAN'),
('ROLE_CASHIER');

-- Users (password is 'password' encoded with BCrypt)
INSERT IGNORE INTO users (username, email, password, first_name, last_name, enabled, created_at, updated_at)
VALUES 
('admin', 'admin@coffeeshop.com', '$2a$10$ZuGgeoawgOg.6AM3QEGZ3O4QlBSWyRx3A70oIP0GG/IJGBF3WqOCK', 'Admin', 'User', true, NOW(), NOW()),
('server', 'server@coffeeshop.com', '$2a$10$ZuGgeoawgOg.6AM3QEGZ3O4QlBSWyRx3A70oIP0GG/IJGBF3WqOCK', 'Server', 'User', true, NOW(), NOW()),
('barman', 'barman@coffeeshop.com', '$2a$10$ZuGgeoawgOg.6AM3QEGZ3O4QlBSWyRx3A70oIP0GG/IJGBF3WqOCK', 'Barman', 'User', true, NOW(), NOW()),
('cashier', 'cashier@coffeeshop.com', '$2a$10$ZuGgeoawgOg.6AM3QEGZ3O4QlBSWyRx3A70oIP0GG/IJGBF3WqOCK', 'Cashier', 'User', true, NOW(), NOW());

-- User Roles
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN';
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'server' AND r.name = 'ROLE_SERVER';
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'barman' AND r.name = 'ROLE_BARMAN';
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'cashier' AND r.name = 'ROLE_CASHIER';

-- Tables
INSERT IGNORE INTO dining_tables (table_number, capacity, status) VALUES
(1, 4, 'AVAILABLE'),
(2, 4, 'AVAILABLE'),
(3, 6, 'AVAILABLE'),
(4, 2, 'AVAILABLE'),
(5, 8, 'AVAILABLE'),
(6, 4, 'AVAILABLE');

-- Products
INSERT IGNORE INTO products (name, description, category, price, stock_quantity, available, created_at, updated_at)
VALUES 
('Espresso', 'Strong black coffee in small serving', 'Coffee', 2.50, 100, true, NOW(), NOW()),
('Cappuccino', 'Espresso with steamed milk foam', 'Coffee', 3.50, 100, true, NOW(), NOW()),
('Latte', 'Espresso with steamed milk', 'Coffee', 3.50, 100, true, NOW(), NOW()),
('Green Tea', 'Traditional green tea', 'Tea', 2.50, 100, true, NOW(), NOW()),
('Croissant', 'Buttery, flaky pastry', 'Pastries', 2.00, 50, true, NOW(), NOW()),
('Chocolate Muffin', 'Fresh baked chocolate muffin', 'Pastries', 2.50, 50, true, NOW(), NOW()); 