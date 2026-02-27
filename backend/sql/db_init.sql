-- 创建数据库
CREATE DATABASE IF NOT EXISTS barley_wine_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE barley_wine_system;

-- 用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    role_id INT,
    status INT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 角色表
CREATE TABLE IF NOT EXISTS role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 权限表
CREATE TABLE IF NOT EXISTS permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    permission_name VARCHAR(50) NOT NULL,
    permission_code VARCHAR(50) NOT NULL,
    url VARCHAR(200),
    parent_id INT DEFAULT 0,
    type INT DEFAULT 1,
    sort INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 种植基地表
CREATE TABLE IF NOT EXISTS planting_base (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    base_name VARCHAR(100) NOT NULL,
    location VARCHAR(200),
    area DOUBLE,
    soil_type VARCHAR(100),
    climate VARCHAR(100),
    manager VARCHAR(50),
    contact_phone VARCHAR(20),
    status INT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 原料采购表
CREATE TABLE IF NOT EXISTS purchase (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    purchase_no VARCHAR(50) NOT NULL,
    supplier_name VARCHAR(100) NOT NULL,
    material_name VARCHAR(100) NOT NULL,
    quantity DOUBLE,
    unit VARCHAR(20),
    price DECIMAL(10,2),
    total_amount DECIMAL(10,2),
    purchase_date DATE,
    status INT DEFAULT 1,
    remark VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 库存表
CREATE TABLE IF NOT EXISTS inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL,
    product_code VARCHAR(50) NOT NULL,
    category VARCHAR(50),
    quantity DOUBLE,
    unit VARCHAR(20),
    price DECIMAL(10,2),
    total_value DECIMAL(10,2),
    warehouse VARCHAR(100),
    status INT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 生产流程表
CREATE TABLE IF NOT EXISTS production_process (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    process_no VARCHAR(50) NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    batch_no VARCHAR(50) NOT NULL,
    quantity DOUBLE,
    unit VARCHAR(20),
    start_date DATE,
    end_date DATE,
    status VARCHAR(50),
    operator VARCHAR(50),
    remark VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 质量管理表
CREATE TABLE IF NOT EXISTS quality_control (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quality_no VARCHAR(50) NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    batch_no VARCHAR(50) NOT NULL,
    test_item VARCHAR(100),
    test_result VARCHAR(500),
    test_standard VARCHAR(500),
    tester VARCHAR(50),
    test_date DATE,
    status VARCHAR(50),
    remark VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 订单表
CREATE TABLE IF NOT EXISTS `order` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    customer_phone VARCHAR(20),
    address VARCHAR(200),
    total_amount DECIMAL(10,2),
    order_date DATE,
    status VARCHAR(50),
    payment_method VARCHAR(50),
    remark VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 客户表
CREATE TABLE IF NOT EXISTS customer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    address VARCHAR(200),
    business_scope VARCHAR(500),
    status INT DEFAULT 1,
    remark VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 角色权限关系表
CREATE TABLE IF NOT EXISTS role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 配送管理表
CREATE TABLE IF NOT EXISTS delivery (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    delivery_no VARCHAR(50) NOT NULL,
    order_no VARCHAR(50) NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    delivery_company VARCHAR(100),
    tracking_no VARCHAR(100),
    delivery_date DATE,
    status VARCHAR(50),
    remark VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 协同任务表
CREATE TABLE IF NOT EXISTS task_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_title VARCHAR(200) NOT NULL,
    task_content VARCHAR(1000),
    assignee VARCHAR(50),
    due_date DATE,
    priority VARCHAR(20),
    status VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 文档管理表
CREATE TABLE IF NOT EXISTS document_file (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    file_name VARCHAR(200) NOT NULL,
    file_url VARCHAR(500),
    uploader VARCHAR(50),
    version VARCHAR(50),
    status INT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 通知公告表
CREATE TABLE IF NOT EXISTS notice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    notice_title VARCHAR(200) NOT NULL,
    notice_content VARCHAR(2000),
    publisher VARCHAR(50),
    is_top INT DEFAULT 0,
    status INT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 质量追溯记录表
CREATE TABLE IF NOT EXISTS trace_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trace_code VARCHAR(100) NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    product_code VARCHAR(50) NOT NULL,
    batch_no VARCHAR(50) NOT NULL,
    stage VARCHAR(100),
    stage_detail VARCHAR(1000),
    operator VARCHAR(50),
    record_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Seed data
INSERT INTO role (role_name, description) VALUES
('admin', 'system admin'),
('planting_manager', 'planting manager'),
('production_manager', 'production manager'),
('sales_manager', 'sales manager');

INSERT INTO user (username, password, real_name, phone, email, role_id, status) VALUES
('admin', '123456', 'Admin', '13800138000', 'admin@example.com', 1, 1),
('planting', '123456', 'Planting Manager', '13800138001', 'planting@example.com', 2, 1),
('production', '123456', 'Production Manager', '13800138002', 'production@example.com', 3, 1),
('sales', '123456', 'Sales Manager', '13800138003', 'sales@example.com', 4, 1);

INSERT INTO planting_base (base_name, location, area, soil_type, climate, manager, contact_phone, status) VALUES
('Base-1', 'Huzhu-WeiYuan', 5000, 'sandy loam', 'plateau continental', 'Zhang', '13900139001', 1),
('Base-2', 'Huzhu-DanMa', 3000, 'loam', 'plateau continental', 'Li', '13900139002', 1),
('Base-3', 'Huzhu-DongHe', 4500, 'sandy loam', 'plateau continental', 'Wang', '13900139003', 1);

INSERT INTO purchase (purchase_no, supplier_name, material_name, quantity, unit, price, total_amount, purchase_date, status, remark) VALUES
('P20260226001', 'Qinghai Cooperative', 'barley', 500, 'ton', 3000, 1500000, '2026-02-26', 1, 'spring purchase'),
('P20260226002', 'Huzhu Agro Co', 'fertilizer', 100, 'ton', 2000, 200000, '2026-02-26', 1, 'spring use'),
('P20260226003', 'Qinghai Machinery', 'pesticide', 50, 'ton', 15000, 750000, '2026-02-26', 1, 'spring disease control');

INSERT INTO inventory (product_name, product_code, category, quantity, unit, price, total_value, warehouse, status) VALUES
('barley', 'QK20260226', 'raw', 1000, 'ton', 3000, 3000000, 'raw warehouse', 1),
('barley wine 500ml', 'QKJ20260226', 'finished', 5000, 'bottle', 50, 250000, 'finished warehouse', 1),
('barley wine 1000ml', 'QKJ20260227', 'finished', 3000, 'bottle', 80, 240000, 'finished warehouse', 1);

INSERT INTO production_process (process_no, product_name, batch_no, quantity, unit, start_date, end_date, status, operator, remark) VALUES
('PP20260226001', 'barley wine', 'B20260226001', 1000, 'ton', '2026-02-20', '2026-02-26', 'done', 'Operator-A', 'spring batch'),
('PP20260226002', 'barley wine', 'B20260226002', 800, 'ton', '2026-02-22', '2026-02-28', 'processing', 'Operator-B', 'spring batch'),
('PP20260226003', 'barley wine', 'B20260226003', 1200, 'ton', '2026-02-25', '2026-03-02', 'pending', 'Operator-C', 'spring batch');

INSERT INTO quality_control (quality_no, product_name, batch_no, test_item, test_result, test_standard, tester, test_date, status, remark) VALUES
('QC20260226001', 'barley wine', 'B20260226001', 'alcohol', '52', '50-55', 'Tester-A', '2026-02-26', 'qualified', 'ok'),
('QC20260226002', 'barley wine', 'B20260226001', 'total acid', '0.5g/L', '<=1.0g/L', 'Tester-A', '2026-02-26', 'qualified', 'ok'),
('QC20260226003', 'barley wine', 'B20260226001', 'total ester', '2.5g/L', '>=2.0g/L', 'Tester-A', '2026-02-26', 'qualified', 'ok');

INSERT INTO `order` (order_no, customer_name, customer_phone, address, total_amount, order_date, status, payment_method, remark) VALUES
('O20260226001', 'Huzhu Barley Co', '0972-1234567', 'Huzhu-WeiYuan', 120000, '2026-02-26', 'done', 'bank', 'bulk'),
('O20260226002', 'Xining Dealer', '0971-1234567', 'Xining-East', 85000, '2026-02-26', 'processing', 'wechat', 'normal'),
('O20260226003', 'Lanzhou Trade', '0931-1234567', 'Lanzhou-Chengguan', 150000, '2026-02-26', 'pending_payment', 'bank', 'bulk');

INSERT INTO customer (customer_name, contact_person, phone, email, address, business_scope, status, remark) VALUES
('Huzhu Barley Co', 'Zhang', '0972-1234567', 'contact@huzhuwine.com', 'Huzhu-WeiYuan', 'wine', 1, 'core'),
('Xining Dealer', 'Li', '0971-1234567', 'contact@xiningdealer.com', 'Xining-East', 'retail', 1, 'normal'),
('Lanzhou Trade', 'Wang', '0931-1234567', 'contact@lanzhoutrade.com', 'Lanzhou-Chengguan', 'trade', 1, 'important');

INSERT INTO permission (permission_name, permission_code, url, parent_id, type, sort) VALUES
('user', 'system:user', '/users', 0, 1, 1),
('role', 'system:role', '/roles', 0, 1, 2),
('permission', 'system:permission', '/permissions', 0, 1, 3),
('planting', 'supply:planting', '/planting-bases', 0, 1, 4),
('purchase', 'supply:purchase', '/purchases', 0, 1, 5),
('inventory', 'supply:inventory', '/inventories', 0, 1, 6),
('production', 'production:process', '/production-processes', 0, 1, 7),
('quality', 'production:quality', '/quality-controls', 0, 1, 8),
('order', 'sales:order', '/orders', 0, 1, 9),
('delivery', 'sales:delivery', '/deliveries', 0, 1, 10),
('customer', 'sales:customer', '/customers', 0, 1, 11),
('task', 'collab:task', '/tasks', 0, 1, 12),
('document', 'collab:document', '/documents', 0, 1, 13),
('notice', 'collab:notice', '/notices', 0, 1, 14),
('trace', 'trace:record', '/trace-records', 0, 1, 15),
('analysis', 'analysis:view', '/analysis', 0, 1, 16);

INSERT INTO role_permission (role_id, permission_id)
SELECT 1, id FROM permission;

INSERT INTO delivery (delivery_no, order_no, customer_name, delivery_company, tracking_no, delivery_date, status, remark) VALUES
('D20260226001', 'O20260226001', 'Huzhu Barley Co', 'SF Express', 'SF123456789CN', '2026-02-27', 'in_transit', 'eta next day'),
('D20260226002', 'O20260226002', 'Xining Dealer', 'JD Logistics', 'JD987654321CN', '2026-02-26', 'signed', 'received');

INSERT INTO task_item (task_title, task_content, assignee, due_date, priority, status) VALUES
('spring purchase review', 'review cross-team purchase plan', 'purchase leader', '2026-03-01', 'high', 'processing'),
('fermentation check', 'check temp and hygiene logs', 'production leader', '2026-02-28', 'medium', 'pending'),
('sales payment tracking', 'track key customer payment', 'sales leader', '2026-03-03', 'high', 'done');

INSERT INTO document_file (title, file_name, file_url, uploader, version, status) VALUES
('process spec', 'process-spec-v1.pdf', '/files/process-spec-v1.pdf', 'production leader', 'v1.0', 1),
('quality sop', 'quality-sop-v2.pdf', '/files/quality-sop-v2.pdf', 'quality leader', 'v2.1', 1),
('purchase template', 'purchase-contract.docx', '/files/purchase-contract.docx', 'purchase leader', 'v1.3', 1);

INSERT INTO notice (notice_title, notice_content, publisher, is_top, status) VALUES
('system online', 'the system is online for trial', 'admin', 1, 1),
('quality reminder', 'complete all in-process sampling this week', 'quality leader', 0, 1);

INSERT INTO trace_record (trace_code, product_name, product_code, batch_no, stage, stage_detail, operator, record_time) VALUES
('TR20260226001', 'barley wine 500ml', 'QKJ20260226', 'B20260226001', 'raw in', 'raw barley passed inspection', 'warehouse', '2026-02-20 09:30:00'),
('TR20260226001', 'barley wine 500ml', 'QKJ20260226', 'B20260226001', 'production', 'fermentation completed', 'production', '2026-02-24 15:20:00'),
('TR20260226001', 'barley wine 500ml', 'QKJ20260226', 'B20260226001', 'quality release', 'all metrics passed', 'quality', '2026-02-26 10:45:00');
